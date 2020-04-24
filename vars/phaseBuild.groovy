import hudson.Functions

import org.ods.scheduler.*
import org.ods.service.*
import org.ods.usecase.*
import org.ods.util.*

def call(Project project, List<Set<Map>> repos) {
    try {
        def steps = ServiceRegistry.instance.get(PipelineSteps)
        def jira = ServiceRegistry.instance.get(JiraUseCase)
        def junit = ServiceRegistry.instance.get(JUnitTestReportsUseCase)
        def util = ServiceRegistry.instance.get(MROPipelineUtil)
        def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler)

        def phase = MROPipelineUtil.PipelinePhases.BUILD

        def globalData = [
            tests: [
                unit: [
                    testReportFiles: [],
                    testResults    : [:]
                ]
            ]
        ]

        def preExecuteRepo = { steps_, repo ->
            levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, repo)
        }

        def postExecuteRepo = { steps_, repo ->
            // FIXME: we are mixing a generic scheduler capability with a data dependency and an explicit repository constraint.
            // We should turn the last argument 'data' of the scheduler into a closure that return data.
            if (project.isAssembleMode && repo.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE) {
                def data = [
                    tests: [
                        unit: getUnitTestResults(steps, repo)
                    ]
                ]

                levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, repo, data)

                jira.reportTestResultsForComponent("Technology-${repo.id}", [Project.TestType.UNIT], data.tests.unit.testResults)

                globalData.tests.unit.testReportFiles.addAll(data.tests.unit.testReportFiles)
            }
        }

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        // Execute phase for each repository
        util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
            .each { group ->
                group.failFast = true
                parallel(group)
            }

        // Parse all test report files into a single data structure
        globalData.tests.unit.testResults = junit.parseTestReportFiles(globalData.tests.unit.testReportFiles)

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)
    } catch (e) {
        // Check for random null references which occur after a Jenkins restart
        if (ServiceRegistry.instance == null || ServiceRegistry.instance.get(PipelineSteps) == null) {
            e = new IllegalStateException("Error: invalid references have been detected for critical pipeline services. Most likely, your Jenkins instance has been recycled. Please re-run the pipeline!").initCause(e)
        }

        echo(e.message)

        try {
            project.reportPipelineStatus(e.message, true)
        } catch (reportError) {
            echo("Error: unable to report pipeline status because of: ${reportError.message}.")
            reportError.initCause(e)
            throw reportError
        }

        throw e
    }
}

private List getUnitTestResults(def steps, Map repo) {
    def jenkins = ServiceRegistry.instance.get(JenkinsService)
    def junit = ServiceRegistry.instance.get(JUnitTestReportsUseCase)

    def testReportsPath = "${PipelineUtil.XUNIT_DOCUMENTS_BASE_DIR}/${repo.id}/unit"

    steps.echo("Collecting JUnit XML Reports for ${repo.id}")
    def testReportsStashName = "test-reports-junit-xml-${repo.id}-${steps.env.BUILD_ID}"
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
        testResults    : junit.parseTestReportFiles(testReportFiles)
    ]
}

return this
