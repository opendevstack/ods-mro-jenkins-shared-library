import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.ServiceRegistry
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineUtil

def call(Map project, List<Set<Map>> repos) {
    // def levaDoc = ServiceRegistry.instance.get(LeVaDocumentUseCase.class.name)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler.class.name)
    def util             = ServiceRegistry.instance.get(PipelineUtil.class.name)

    def phase = MROPipelineUtil.PipelinePhases.TEST

    def preExecuteRepo = { steps, repo ->
        /*
        // Software Development (Coding and Code Review) Report
        if (LeVaDocumentUseCase.appliesToRepo(repo, LeVaDocumentUseCase.DocumentTypes.SCR, phase)) {
            echo "Creating and archiving a Software Development (Coding and Code Review) Report for repo '${repo.id}'"
            levaDoc.createSCR(project, repo)
        }
        */

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, project, repo)
    }

    def postExecuteRepo = { steps, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, project, repo)
    }

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START, project)

    // Execute phase for each repository
    util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
        .each { group ->
            parallel(group)
        }

    /*
    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.SCR, phase)) {
        echo "Creating and archiving an overall Software Development (Coding and Code Review) Report for project '${project.id}'"
        levaDoc.createOverallSCR(project)
    }
    */

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project)
}

return this
