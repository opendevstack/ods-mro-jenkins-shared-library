import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.JenkinsService
import org.ods.service.ServiceRegistry
import org.ods.usecase.JUnitTestReportsUseCase
import org.ods.usecase.JiraUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineUtil

import groovy.json.JsonOutput

def call(Map project, List<Set<Map>> repos) {
    def jira             = ServiceRegistry.instance.get(JiraUseCase.class.name)
    def junit            = ServiceRegistry.instance.get(JUnitTestReportsUseCase.class.name)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler.class.name)
    def util             = ServiceRegistry.instance.get(PipelineUtil.class.name)

    def phase = MROPipelineUtil.PipelinePhases.TEST

    def data = [
        tests: [
            installation: [
                testReportFiles: [],
                testResults: [:]
            ],
            functional: [
                testReportFiles: [],
                testResults: [:]
            ]
        ]
    ]

    def preExecuteRepo = { steps, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, project, repo)
    }

    def postExecuteRepo = { steps, repo ->
        if (repo.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_TEST) {
            // Add installation test report files to a global data structure
            def installationTestResults = getInstallationTestResults(steps, repo)
            data.tests.installation.testReportFiles.addAll(installationTestResults.testReportFiles)

            // Add functional test report files to a global data structure
            def functionalTestResults = getFunctionalTestResults(steps, repo)
            data.tests.functional.testReportFiles.addAll(functionalTestResults.testReportFiles)

            project.repositories.each { repo_ ->
                if (repo_.type?.toLowerCase() != MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_TEST) {
                    echo "Reporting installation test results to corresponding test cases in Jira for ${repo_.id}"
                    jira.reportTestResultsForComponent(project.id, "Technology-${repo_.id}", ["InstallationTest"], installationTestResults.testResults)

                    echo "Reporting functional test results to corresponding test cases in Jira for ${repo_.id}"
                    jira.reportTestResultsForComponent(project.id, "Technology-${repo_.id}", ["AcceptanceTest", "IntegrationTest"], functionalTestResults.testResults)
                }
            }

            levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, project, repo)
        }
    }

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START, project)

    // Execute phase for each repository
    util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
        .each { group ->
            parallel(group)
        }

    // Parse all test report files into a single data structure
    data.tests.functional.testResults = junit.parseTestReportFiles(data.tests.functional.testReportFiles)
    data.tests.installation.testResults = junit.parseTestReportFiles(data.tests.installation.testReportFiles)

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, [:], data)
}

private List getFunctionalTestResults(def steps, Map repo) {
    def junit = ServiceRegistry.instance.get(JUnitTestReportsUseCase.class.name)

    def acceptanceTestResults = this.getTestResults(steps, repo, "acceptance")
    echo("!!! acceptanceTestResults: ${JsonOutput.toJson(acceptanceTestResults)}")
    def integrationTestResults = this.getTestResults(steps, repo, "integration")
    echo("!!! integrationTestResults: ${JsonOutput.toJson(integrationTestResults)}")

    def testReportFiles = []
    testReportFiles.addAll(acceptanceTestResults.testReportFiles)
    testReportFiles.addAll(integrationTestResults.testReportFiles)

    return [
        testReportFiles: testReportFiles,
        testResults: junit.parseTestReportFiles(testReportFiles)
    ]
}

private List getInstallationTestResults(def steps, Map repo) {
    return this.getTestResults(steps, repo, "installation")
}

private List getTestResults(def steps, Map repo, String type) {
    def jenkins = ServiceRegistry.instance.get(JenkinsService.class.name)
    def junit   = ServiceRegistry.instance.get(JUnitTestReportsUseCase.class.name)

    def testReportsPath = "junit/${repo.id}/${type}"

    echo "Collecting JUnit XML Reports for ${repo.id}"
    def testReportsStashName = "${type}-test-reports-junit-xml-${repo.id}-${steps.env.BUILD_ID}"
    def testReportsUnstashPath = "${steps.env.WORKSPACE}/${testReportsPath}"
    def hasStashedTestReports = jenkins.unstashFilesIntoPath(testReportsStashName, testReportsUnstashPath, "JUnit XML Report")
    if (!hasStashedTestReports) {
        throw new RuntimeException("Error: unable to unstash JUnit XML reports for repo '${repo.id}' from stash '${testReportsStashName}'.")
    }

    def testReportFiles = junit.loadTestReportsFromPath(testReportsUnstashPath)
    echo("!!! testReportFiles: ${testReportFiles.collect { it.canonicalPath }}")

    return [
        // Load JUnit test report files from path
        testReportFiles: testReportFiles,
        // Parse JUnit test report files into a report
        testResults: junit.parseTestReportFiles(testReportFiles)
    ]
}

return this
