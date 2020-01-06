package org.ods.scheduler

import org.ods.usecase.DocGenUseCase
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil

abstract class DocGenScheduler {

    protected IPipelineSteps steps
    protected DocGenUseCase usecase

    DocGenScheduler(IPipelineSteps steps, DocGenUseCase usecase) {
        this.steps = steps
        this.usecase = usecase
    }

    protected abstract boolean isDocumentApplicable(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project, Map repo = null)

    void run(String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project, Map repo = null, Map data = null) {
        def documents = this.usecase.getSupportedDocuments()
        documents.each { documentType, method ->
            def args = [project, repo, data]
            def argsDefined = args.findAll()

            def params = method.getParameterTypes()
            if (params.size() == 0 || argsDefined.size() > params.size()) {
                return
            }

            if (this.isDocumentApplicable(documentType, phase, stage, project, repo)) {
                def message = "Creating document of type '${documentType}' for project ${project.id}"
                if (repo) {
                    message += " and repo '${repo.id}'"
                }

                this.steps.echo()
                // Apply args according to the method's parameters length
                method.doMethodInvoke(this.usecase, args[0..(Math.min(args.size(), params.size()) - 1)] as Object[])
            }
        }
    }
}
