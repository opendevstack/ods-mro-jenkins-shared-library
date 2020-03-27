import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.ServiceRegistry
import org.ods.util.MROPipelineUtil
import org.ods.util.PipelineSteps
import org.ods.util.Project

def call(Project project, List<Set<Map>> repos) {
    def steps = new PipelineSteps(this)
    def levaDocScheduler = ServiceRegistry.instance.get(LeVADocumentScheduler)
    def util             = ServiceRegistry.instance.get(MROPipelineUtil)

    def phase = MROPipelineUtil.PipelinePhases.RELEASE

    def preExecuteRepo = { steps_, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO, repo)
    }

    def postExecuteRepo = { steps_, repo ->
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, repo)
    }

    try {
        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        util.prepareExecutePhaseForReposNamedJob(phase, repos, preExecuteRepo, postExecuteRepo)
            .each { group ->
                parallel(group)
            }

        levaDocScheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)
    } catch (e) {
        steps.echo(e.message)
        project.reportPipelineStatus(e)
        throw e
    }
}

return this
