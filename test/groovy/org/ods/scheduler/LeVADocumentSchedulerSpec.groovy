package org.ods.scheduler

import org.ods.service.DocGenService
import org.ods.service.JenkinsService
import org.ods.service.LeVADocumentChaptersFileService
import org.ods.service.NexusService
import org.ods.service.OpenShiftService
import org.ods.usecase.DocGenUseCase
import org.ods.usecase.JiraUseCase
import org.ods.usecase.LeVADocumentUseCase
import org.ods.usecase.SonarQubeUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PDFUtil

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class LeVADocumentSchedulerSpec extends SpecHelper {

    static def PROJECT_GAMP_1
    static def PROJECT_GAMP_3
    static def PROJECT_GAMP_4
    static def PROJECT_GAMP_5
    static def PROJECT_GAMP_5_WITHOUT_JIRA
    static def PROJECT_GAMP_5_WITHOUT_REPOS

    static def REPO_ODS_CODE
    static def REPO_ODS_SERVICE
    static def REPO_ODS_TEST

    def setupSpec() {
        def project = createProject()

        REPO_ODS_CODE = project.repositories[0]
        REPO_ODS_CODE.type = MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE

        REPO_ODS_SERVICE = project.repositories[1]
        REPO_ODS_SERVICE.type = MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_SERVICE

        REPO_ODS_TEST = project.repositories[2]
        REPO_ODS_TEST.type = MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_TEST

        PROJECT_GAMP_1 = createProject()
        PROJECT_GAMP_1.capabilities << [ LeVADocs: [ GAMPCategory: "1" ] ]

        PROJECT_GAMP_3 = createProject()
        PROJECT_GAMP_3.capabilities << [ LeVADocs: [ GAMPCategory: "3" ] ]

        PROJECT_GAMP_4 = createProject()
        PROJECT_GAMP_4.capabilities << [ LeVADocs: [ GAMPCategory: "4" ] ]

        PROJECT_GAMP_5 = createProject()
        PROJECT_GAMP_5.capabilities << [ LeVADocs: [ GAMPCategory: "5" ] ]

        PROJECT_GAMP_5_WITHOUT_JIRA = createProject()
        PROJECT_GAMP_5_WITHOUT_JIRA.capabilities << [ LeVADocs: [ GAMPCategory: "5" ] ]
        PROJECT_GAMP_5_WITHOUT_JIRA.services.jira = null

        PROJECT_GAMP_5_WITHOUT_REPOS = createProject()
        PROJECT_GAMP_5_WITHOUT_REPOS.capabilities << [ LeVADocs: [ GAMPCategory: "5" ] ]
        PROJECT_GAMP_5_WITHOUT_REPOS.repositories = []
    }

    @Unroll
    def "is document applicable for GAMP category 1"() {
        given:
        def project = PROJECT_GAMP_1

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                        | repo | phase                                   | stage                                                         || result
        // CSD: Configuration Specification
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

              // DTP: Software Development Testing Plan
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTR: Software Development Testing Report
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTP: Combined Functional and Requirements Testing Plan
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTR: Combined Functional and Requirements Testing Report
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVP: Configuration and Installation Testing Plan
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVR: Configuration and Installation Testing Report
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // RA: Risk Assessment
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // SSDS: Software Design Specification
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIP: Technical Installation Plan
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIR: Technical Installation Report
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_DTR: Overall Software Development Testing Report
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_TIR: Overall Technical Installation Report
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true

        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
    }

    @Unroll
    def "is document applicable for GAMP category 3"() {
        given:
        def project = PROJECT_GAMP_3

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                        | repo | phase                                   | stage                                                         || result
        // CSD: Configuration Specification
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START          || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

         // DTP: Software Development Testing Plan
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTR: Software Development Testing Report
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTP: Combined Functional and Requirements Testing Plan
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTR: Combined Functional and Requirements Testing Report
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVP: Configuration and Installation Testing Plan
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVR: Configuration and Installation Testing Report
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // RA: Risk Assessment
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // SSDS: Software Design Specification
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIP: Technical Installation Plan
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIR: Technical Installation Report
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_DTR: Overall Software Development Testing Report
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_TIR: Overall Technical Installation Report
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true

        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
    }

    @Unroll
    def "is document applicable for GAMP category 4"() {
        given:
        def project = PROJECT_GAMP_4

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                        | repo | phase                                   | stage                                                         || result
        // CSD: Configuration Specification
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START          || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTP: Software Development Testing Plan
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTR: Software Development Testing Report
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTP: Combined Functional and Requirements Testing Plan
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTR: Combined Functional and Requirements Testing Report
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVP: Configuration and Installation Testing Plan
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVR: Configuration and Installation Testing Report
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // RA: Risk Assessment
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // SSDS: Software Design Specification
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIP: Technical Installation Plan
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIR: Technical Installation Report
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_DTR: Overall Software Development Testing Report
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_TIR: Overall Technical Installation Report
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true

        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
    }

    @Unroll
    def "is document applicable for GAMP category 5"() {
        given:
        def project = PROJECT_GAMP_5

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                        | repo | phase                                   | stage                                                         || result
        // CSD: Configuration Specification
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CSD | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTP: Software Development Testing Plan
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // DTR: Software Development Testing Report
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTP: Combined Functional and Requirements Testing Plan
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // CFTR: Combined Functional and Requirements Testing Report
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.CFTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVP: Configuration and Installation Testing Plan
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // IVR: Configuration and Installation Testing Report
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.IVR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // RA: Risk Assessment
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.RA | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // SSDS: Software Design Specification
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.SSDS | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIP: Technical Installation Plan
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || true
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIP | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // TIR: Technical Installation Report
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || true
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_DTR: Overall Software Development Testing Report
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false

        // OVERALL_TIR: Overall Technical Installation Report
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || true

        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_CODE    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_SERVICE | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.INIT     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.DEPLOY   | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.RELEASE  | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START        || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO  || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | REPO_ODS_TEST    | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END           || false
    }

    def "is document applicable with invalid GAMP category"() {
        given:
        def project = createProject()
        project.capabilities << [ LeVADocs: [ GAMPCategory: "0" ] ]

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def documentType = "myDocumentType"
        def phase = "myPhase"
        def stage = MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START
        def repo = project.repositories.first()

        when:
        scheduler.isDocumentApplicable(documentType, phase, stage)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to assert applicability of document type '${documentType}' for project '${project.key}' in phase '${phase}'. The GAMP category '0' is not supported."

        when:
        scheduler.isDocumentApplicable(documentType, phase, stage, repo)

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to assert applicability of document type '${documentType}' for project '${project.key}' and repo '${repo.id}' in phase '${phase}'. The GAMP category '0' is not supported."
    }

    def "is document applicable in the absence of Jira"() {
        given:
        def project = PROJECT_GAMP_5_WITHOUT_JIRA

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                         | repo | phase                               | stage                                               || result
        LeVADocumentUseCase.DocumentType.CSD | null | MROPipelineUtil.PipelinePhases.INIT | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END || false
    }

    def "is document applicable in the absence of repositories"() {
        given:
        def project = PROJECT_GAMP_5_WITHOUT_REPOS

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def usecase = Mock(LeVADocumentUseCase)
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        expect:
        scheduler.isDocumentApplicable(documentType as String, phase, stage, repo) == result

        where:
        documentType                                 | repo | phase                                   | stage                                               || result
        LeVADocumentUseCase.DocumentType.CFTR         | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END || false
        LeVADocumentUseCase.DocumentType.IVR         | null | MROPipelineUtil.PipelinePhases.TEST     | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END || false
        LeVADocumentUseCase.DocumentType.OVERALL_DTR | null | MROPipelineUtil.PipelinePhases.BUILD    | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END || false
        LeVADocumentUseCase.DocumentType.OVERALL_TIR | null | MROPipelineUtil.PipelinePhases.FINALIZE | MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END || false
    }

    def "run for GAMP category 1 in DEV"() {
        given:
        def project = PROJECT_GAMP_1
        project.buildParams.targetEnvironment = "dev"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()


        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.INIT, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createCSD", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createTIP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createCFTP", [null, null] as Object[])
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createRA", [[:], data] as Object[])
        1 * usecase.invokeMethod("createSSDS", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.FINALIZE, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createOverallTIR", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 3 in DEV"() {
        given:
        def project = PROJECT_GAMP_3
        project.buildParams.targetEnvironment = "dev"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.INIT, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createCSD", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createRA", [[:], data] as Object[])
        1 * usecase.invokeMethod("createSSDS", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 4 in DEV"() {
        given:
        def project = PROJECT_GAMP_4
        project.buildParams.targetEnvironment = "dev"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }

            invokeMethod(_, _) >> { method, args ->
                if (method.startsWith("create")) {
                    return "http://nexus"
                }
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.INIT, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createCSD", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createCFTP", [null, null] as Object[])
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createRA", [[:], data] as Object[])
        1 * usecase.invokeMethod("createSSDS", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 5 in DEV"() {
        given:
        def project = PROJECT_GAMP_5
        project.buildParams.targetEnvironment = "dev"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.INIT, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createCSD", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.BUILD, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createDTP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.BUILD, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createOverallDTR", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.BUILD, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE, data)

        then:
        1 * usecase.invokeMethod("createDTR", [REPO_ODS_CODE, data] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.BUILD, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, REPO_ODS_CODE)

        then:
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.BUILD, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, REPO_ODS_TEST)

        then:
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createTIP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createCFTP", [null, null] as Object[])
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createDIL", [[:], data] as Object[])
        1 * usecase.invokeMethod("createRA", [[:], data] as Object[])
        1 * usecase.invokeMethod("createSSDS", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createDIL", [[:], data] as Object[])
        1 * usecase.invokeMethod("createRA", [[:], data] as Object[])
        1 * usecase.invokeMethod("createSSDS", [[:], data] as Object[])
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.FINALIZE, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        1 * usecase.invokeMethod("createOverallTIR", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 1 in QA"() {
        given:
        def project = PROJECT_GAMP_1
        project.buildParams.targetEnvironment = "qa"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createTIP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 3 in QA"() {
        given:
        def project = PROJECT_GAMP_3
        project.buildParams.targetEnvironment = "qa"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 4 in QA"() {
        given:
        def project = PROJECT_GAMP_4
        project.buildParams.targetEnvironment = "qa"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)
 
        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 5 in QA"() {
        given:
        def project = PROJECT_GAMP_5
        project.buildParams.targetEnvironment = "qa"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createTIP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START)

        then:
        1 * usecase.invokeMethod("createIVP", [null, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        1 * usecase.invokeMethod("createDIL", [[:], data] as Object[])
        1 * usecase.invokeMethod("createCFTR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 1 in PROD"() {
        given:
        def project = PROJECT_GAMP_1
        project.buildParams.targetEnvironment = "prod"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 3 in PROD"() {
        given:
        def project = PROJECT_GAMP_3
        project.buildParams.targetEnvironment = "prod"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
    }

    def "run for GAMP category 4 in PROD"() {
        given:
        def project = PROJECT_GAMP_4
        project.buildParams.targetEnvironment = "prod"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "run for GAMP category 5 in PROD"() {
        given:
        def project = PROJECT_GAMP_5
        project.buildParams.targetEnvironment = "prod"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_CODE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_CODE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.DEPLOY, MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO, REPO_ODS_SERVICE)

        then:
        1 * usecase.invokeMethod("createTIR", [REPO_ODS_SERVICE, null] as Object[])
        0 * usecase.invokeMethod(*_)

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.TEST, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END, [:], data)

        then:
        1 * usecase.invokeMethod("createIVR", [[:], data] as Object[])
        0 * usecase.invokeMethod(*_)
    }

    def "in DEV environment all documents types are applicable"() {
        given:
        def project = createProject()

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }
       
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        def result = []
        def expected = []

        when:
        for (LeVADocumentUseCase.DocumentType documentType : LeVADocumentUseCase.DocumentType.values()) {
            result.add(scheduler.isDocumentApplicableForEnvironment(documentType.name(), "D"))
            expected.add(true)
        }

        then:
        _ * scheduler.isDocumentApplicableForEnvironment(_ as String, "D")
        expected == result
    }

    def "in QA environment only specific types are applicable"() {
        given:
        def project = createProject()

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }
       
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def qTypes = [ 
            LeVADocumentUseCase.DocumentType.DTR as String,
            LeVADocumentUseCase.DocumentType.CFTR as String,
            LeVADocumentUseCase.DocumentType.IVP as String,
            LeVADocumentUseCase.DocumentType.IVR as String,
            LeVADocumentUseCase.DocumentType.TIP as String,
            LeVADocumentUseCase.DocumentType.TIR as String,
        ]

        def result = []
        def expected = []

        when:
        for (LeVADocumentUseCase.DocumentType documentType : qTypes) {
            result.add(scheduler.isDocumentApplicableForEnvironment(documentType.name(), "Q"))
            expected.add(true)
        }

        then:
        _ * scheduler.isDocumentApplicableForEnvironment(_ as String, "Q")
        expected == result
    }

    def "in PROD environment only specific types are applicable"() {
        given:
        def project = createProject()

        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jiraUseCase = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jiraUseCase, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }
        }
       
        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def pTypes = [ 
            LeVADocumentUseCase.DocumentType.IVR as String,
            LeVADocumentUseCase.DocumentType.TIR as String
        ]

        def result = []
        def expected = []

        when:
        for (LeVADocumentUseCase.DocumentType documentType : pTypes) {
            result.add(scheduler.isDocumentApplicableForEnvironment(documentType.name(), "P"))
            expected.add(true)
        }

        then:
        _ * scheduler.isDocumentApplicableForEnvironment(_ as String, "P")
        expected == result
    }

    def "run with a failure stops the pipeline"() {
        given:
        def project = PROJECT_GAMP_1
        project.buildParams.targetEnvironment = "dev"
        project.buildParams.targetEnvironmentToken = project.buildParams.targetEnvironment[0].toUpperCase()

        def steps = Spy(PipelineSteps)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVADocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)

        def utilObj = new MROPipelineUtil(project, steps)
        def util = Mock(MROPipelineUtil) {
            executeBlockAndFailBuild(_) >> { block ->
                utilObj.executeBlockAndFailBuild(block)
            }
        }

        def usecaseObj = new LeVADocumentUseCase(project, steps, util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq)
        def usecase = Mock(LeVADocumentUseCase) {
            getMetaClass() >> {
                return usecaseObj.getMetaClass()
            }

            getSupportedDocuments() >> {
                return usecaseObj.getSupportedDocuments()
            }

            invokeMethod(_, _) >> { method, args ->
                if (method.startsWith("create")) {
                    throw new IllegalStateException("some error")
                }
            }
        }

        def scheduler = Spy(new LeVADocumentScheduler(project, steps, util, usecase))

        // Test Parameters
        def data = [ testReportFiles: null, testResults: null ]

        when:
        scheduler.run(MROPipelineUtil.PipelinePhases.INIT, MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Error: Creating document of type 'CSD' for project '${project.key}' in phase '${MROPipelineUtil.PipelinePhases.INIT}' and stage '${MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END}' has failed: some error."
    }
}
