package org.ods.scheduler

import org.ods.service.DocGenService
import org.ods.service.NexusService
import org.ods.usecase.DocGenUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PDFUtil

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class DocGenSchedulerSpec extends SpecHelper {

    class DocGenUseCaseImpl extends DocGenUseCase {
        DocGenUseCaseImpl(PipelineSteps steps, MROPipelineUtil util, DocGenService docGen, NexusService nexus, PDFUtil pdf) {
            super(steps, util, docGen, nexus, pdf)
        }

        void createDocumentTypeA(Map project) {}
        void createDocumentTypeB(Map project, Map repo) {}
        void createDocumentTypeC(Map project, Map repo, Map data) {}

        Map<String, MetaMethod> getSupportedDocuments() {
            return this.metaClass.methods
                .findAll { method ->
                    return method.getName() ==~ /^createDocumentType[A-Z]/
                }
                .collectEntries { method ->
                    def name = method.getName().replaceAll("createDocumentType", "")
                    return [ name, method ]
                }
        }
    }

    class DocGenSchedulerImpl extends DocGenScheduler {
        DocGenSchedulerImpl(PipelineSteps steps, DocGenUseCase docGen) {
            super(steps, docGen)
        }

        protected boolean isDocumentApplicable(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project, Map repo = null) {
            return true
        }
    }

    def "run"() {
        given:
        def steps = Spy(PipelineSteps)
        def usecase = Spy(new DocGenUseCaseImpl(steps, Mock(MROPipelineUtil), Mock(DocGenService), Mock(NexusService), Mock(PDFUtil)))
        def scheduler = Spy(new DocGenSchedulerImpl(steps, usecase))

        // Test Parameters
        def phase = "myPhase"
        def project = createProject()
        def repo = project.repositories.first()
        def data = [ a: 1, b: 2, c: 3 ]

        when:
        scheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project)

        then:
        1 * usecase.getSupportedDocuments()

        then:
        1 * scheduler.isDocumentApplicable("A", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, null)
        1 * usecase.createDocumentTypeA(project)

        then:
        1 * scheduler.isDocumentApplicable("B", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, null)
        1 * usecase.createDocumentTypeB(project, null)

        then:
        1 * scheduler.isDocumentApplicable("C", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, null)
        1 * usecase.createDocumentTypeC(project, null,  null)

        when:
        scheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)

        then:
        1 * usecase.getSupportedDocuments()

        then:
        0 * scheduler.isDocumentApplicable("A", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        0 * usecase.createDocumentTypeA(project)

        then:
        1 * scheduler.isDocumentApplicable("B", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        1 * usecase.createDocumentTypeB(project, repo)

        then:
        1 * scheduler.isDocumentApplicable("C", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        1 * usecase.createDocumentTypeC(project, repo,  null)

        when:
        scheduler.run(phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo, data)

        then:
        1 * usecase.getSupportedDocuments()

        then:
        0 * scheduler.isDocumentApplicable("A", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        0 * usecase.createDocumentTypeA(project)

        then:
        0 * scheduler.isDocumentApplicable("B", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        0 * usecase.createDocumentTypeB(project, repo)

        then:
        1 * scheduler.isDocumentApplicable("C", phase, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, project, repo)
        1 * usecase.createDocumentTypeC(project, repo,  data)
    }
}
