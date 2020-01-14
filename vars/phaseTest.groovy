import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.JenkinsService
import org.ods.service.ServiceRegistry
import org.ods.usecase.JUnitTestReportsUseCase
import org.ods.usecase.JiraUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineUtil

def call(Map project, List<Set<Map>> repos) {
    def jira             = ServiceRegistry.instance.get(JiraUseCase.class.name)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler.class.name)
    def util             = ServiceRegistry.instance.get(PipelineUtil.class.name)

    def phase = MROPipelineUtil.PipelinePhases.TEST

    def data = [
        testResults: [
            installation: [
                testReportFiles: [],
                testResults: []
            ]
        ]
    ]

    def preExecuteRepo = { steps, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, project, repo)
    }

    def postExecuteRepo = { steps, repo ->
        if (repo.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_TEST) {
            // Add installation test results to a global data structure
            def installationTestResults = getInstallationTestResults(steps, repo)
            data.testResults.installation.testReportFiles.addAll(installationTestResults.testReportFiles)
            data.testResults.installation.testResults.addAll(installationTestResults.testResults)

            echo("??? installationTestResults: ${installationTestResults}")

            project.repositories.each { repo_ ->
                if (repo_.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE || repo_.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_SERVICE) {
                    // Report test results to corresponding test cases in Jira
                    echo("??? reporting back test results for repo ${repo.id}")
                    jira.reportTestResultsForComponent(project.id, "Technology-${repo_.id}", "InstallationTest", data.testResults)
                }
            }

            levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, project, repo, data.testResults.installation)
        }
    }

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START, project)

    // Execute phase for each repository
    util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
        .each { group ->
            parallel(group)
        }

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, [:], data)
}

private Map getInstallationTestResults(def steps, Map repo) {
    this.getTestResults(steps, repo, "installation")
}

private Map getTestResults(def steps, Map repo, String type) {
    def jenkins = ServiceRegistry.instance.get(JenkinsService.class.name)
    def junit   = ServiceRegistry.instance.get(JUnitTestReportsUseCase.class.name)

    def testReportsPath = "junit/${repo.id}"

    echo "Collecting JUnit XML Reports for ${repo.id}"
    def testReportsStashName = "${type}-test-reports-junit-xml-${repo.id}-${steps.env.BUILD_ID}"
    def testReportsUnstashPath = "${steps.env.WORKSPACE}/${testReportsPath}"
    def hasStashedTestReports = jenkins.unstashFilesIntoPath(testReportsStashName, testReportsUnstashPath, "JUnit XML Report")
    if (!hasStashedTestReports) {
        throw new RuntimeException("Error: unable to unstash JUnit XML reports for repo '${repo.id}' from stash '${testReportsStashName}'.")
    }

    def testReportFiles = junit.loadTestReportsFromPath(testReportsUnstashPath)

    return [
        // Load JUnit test report files from path
        testReportFiles: testReportFiles,
        // Parse JUnit test report files into a report
        testResults: junit.parseTestReportFiles(testReportFiles)
    ]
}

return this
