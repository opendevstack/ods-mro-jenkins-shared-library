import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.JenkinsService
import org.ods.service.ServiceRegistry
import org.ods.usecase.JUnitTestReportsUseCase
import org.ods.usecase.JiraUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineUtil

def call(Map project, List<Set<Map>> repos) {
    def jenkins          = ServiceRegistry.instance.get(JenkinsService.class.name)
    def jira             = ServiceRegistry.instance.get(JiraUseCase.class.name)
    def junit            = ServiceRegistry.instance.get(JUnitTestReportsUseCase.class.name)
//    def levaDoc = ServiceRegistry.instance.get(LeVADocumentUseCase.class.name)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler.class.name)
    def util             = ServiceRegistry.instance.get(PipelineUtil.class.name)

    def phase = MROPipelineUtil.PipelinePhases.BUILD

    def preExecuteRepo = { steps, repo ->
        /*
        if (LeVaDocumentUseCase.appliesToRepo(repo, LeVaDocumentUseCase.DocumentTypes.SDS, phase)) {
            echo "Creating and archiving a Software Design Specification for repo '${repo.id}'"
            levaDoc.createSDS(project, repo)
        }
        */

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, project, repo)
    }

    def postExecuteRepo = { steps, repo ->
        /*
        // Software Development (Coding and Code Review) Report
        if (LeVaDocumentUseCase.appliesToRepo(repo, LeVaDocumentUseCase.DocumentTypes.SCR, phase)) {
            echo "Creating and archiving a Software Development (Coding and Code Review) Report for repo '${repo.id}'"
            levaDoc.createSCR(project, repo)
        }
        */

        def testReportsPath = "junit/${repo.id}"

        echo "Collecting JUnit XML Reports for ${repo.id}"
        def testReportsStashName = "test-reports-junit-xml-${repo.id}-${steps.env.BUILD_ID}"
        def testReportsUnstashPath = "${steps.env.WORKSPACE}/${testReportsPath}"
        def hasStashedTestReports = jenkins.unstashFilesIntoPath(testReportsStashName, testReportsUnstashPath, "JUnit XML Report")
        if (!hasStashedTestReports) {
            throw new RuntimeException("Error: unable to unstash JUnit XML reports for repo '${repo.id}' from stash '${testReportsStashName}'.")
        }

        // Load JUnit test report files from path
        def testReportFiles = junit.loadTestReportsFromPath(testReportsUnstashPath)

        // Parse JUnit test report files into a report
        def testResults = junit.parseTestReportFiles(testReportFiles)

        /*
        // Software Development Testing Report
        if (LeVaDocumentUseCase.appliesToRepo(repo, LeVaDocumentUseCase.DocumentTypes.DTR, phase)) {
            echo "Creating and archiving a Software Development Testing Report for repo '${repo.id}'"
            levaDoc.createDTR(project, repo, [ testResults: testResults, testReportFiles: testReportFiles ])
        }
        */

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, project, repo, [
            testReportFiles: testReportFiles,
            testResults: testResults
        ])

        // Report test results to corresponding test cases in Jira
        jira.reportTestResultsForComponent(project.id, "Technology_${repo.id}", testResults)            

    }

    /*
    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.SCP, phase)) {
        echo "Creating and archiving a Software Development (Coding and Code Review) Plan for project '${project.id}'"
        levaDoc.createSCP(project)
    }

    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.DTP, phase)) {
        echo "Creating and archiving a Software Development Testing Plan for project '${project.id}'"
        levaDoc.createDTP(project)
    }
    */

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START, project)

    // Execute phase for each repository
    util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
        .each { group ->
            parallel(group)
        }

    /*
    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.DTR, phase)) {
        echo "Creating and archiving an overall Software Development Testing Report for project '${project.id}'"
        levaDoc.createOverallDTR(project)
    }

    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.SDS, phase)) {
        echo "Creating and archiving an overall Software Design Specification for project '${project.id}'"
        levaDoc.createOverallSDS(project)
    }
    */

    levaDocScheduler.run(phase, project, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project)
}

return this
