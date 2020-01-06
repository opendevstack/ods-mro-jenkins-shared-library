import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.ServiceRegistry
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineUtil

def call(Map project, List<Set<Map>> repos) {
    // def levaDoc = ServiceRegistry.instance.get(LeVaDocumentUseCase.class.name)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler.class.name)
    def util             = ServiceRegistry.instance.get(PipelineUtil.class.name)

    def phase = MROPipelineUtil.PipelinePhases.DEPLOY

    def preExecuteRepo = { steps, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, project, repo)
    }

    def postExecuteRepo = { steps, repo ->
        /*
        if (LeVaDocumentUseCase.appliesToRepo(repo, LeVaDocumentUseCase.DocumentTypes.TIR, phase)) {
            echo "Creating and archiving a Technical Installation Report for repo '${repo.id}'"
            levaDoc.createTIR(project, repo)
        }
        */

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, project, repo)
    }

    /*
    if (LeVaDocumentUseCase.appliesToProject(project, LeVaDocumentUseCase.DocumentTypes.TIP, phase)) {
        echo "Creating and archiving a Technical Installation Plan for project '${project.id}'"
        levaDoc.createTIP(project)
    }
    */

    levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START, project)

    // Execute phase for each repository
    util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
        .each { group ->
            parallel(group)
        }

    levaDocScheduler.run(phase, project, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project)
}

return this
