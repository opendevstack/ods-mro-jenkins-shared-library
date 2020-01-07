package org.ods.scheduler

import org.ods.usecase.LeVADocumentUseCase
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil

class LeVADocumentScheduler extends DocGenScheduler {

    // Document types per GAMP category
    private static Map GAMP_CATEGORIES = [
        "1": [
            LeVADocumentUseCase.DocumentTypes.DSD,
            LeVADocumentUseCase.DocumentTypes.FS,
            LeVADocumentUseCase.DocumentTypes.FTP,
            LeVADocumentUseCase.DocumentTypes.FTR,
            LeVADocumentUseCase.DocumentTypes.IVP,
            LeVADocumentUseCase.DocumentTypes.IVR,
            LeVADocumentUseCase.DocumentTypes.TIP,
            LeVADocumentUseCase.DocumentTypes.TIR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_TIR
        ],
        "3": [
            LeVADocumentUseCase.DocumentTypes.DSD,
            LeVADocumentUseCase.DocumentTypes.IVP,
            LeVADocumentUseCase.DocumentTypes.IVR,
            LeVADocumentUseCase.DocumentTypes.URS,
            LeVADocumentUseCase.DocumentTypes.TIP,
            LeVADocumentUseCase.DocumentTypes.TIR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_TIR
        ],
        "4": [
            LeVADocumentUseCase.DocumentTypes.CS,
            LeVADocumentUseCase.DocumentTypes.DSD,
            LeVADocumentUseCase.DocumentTypes.FTP,
            LeVADocumentUseCase.DocumentTypes.FTR,
            LeVADocumentUseCase.DocumentTypes.IVP,
            LeVADocumentUseCase.DocumentTypes.IVR,
            LeVADocumentUseCase.DocumentTypes.URS,
            LeVADocumentUseCase.DocumentTypes.TIP,
            LeVADocumentUseCase.DocumentTypes.TIR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_TIR
        ],
        "5": [
            LeVADocumentUseCase.DocumentTypes.CS,
            LeVADocumentUseCase.DocumentTypes.DSD,
            LeVADocumentUseCase.DocumentTypes.DTP,
            LeVADocumentUseCase.DocumentTypes.DTR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_DTR,
            LeVADocumentUseCase.DocumentTypes.FS,
            LeVADocumentUseCase.DocumentTypes.FTP,
            LeVADocumentUseCase.DocumentTypes.FTR,
            LeVADocumentUseCase.DocumentTypes.IVP,
            LeVADocumentUseCase.DocumentTypes.IVR,
            LeVADocumentUseCase.DocumentTypes.SCP,
            LeVADocumentUseCase.DocumentTypes.SCR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_SCR,
            LeVADocumentUseCase.DocumentTypes.SDS,
            LeVADocumentUseCase.DocumentTypes.OVERALL_SDS,
            LeVADocumentUseCase.DocumentTypes.URS,
            LeVADocumentUseCase.DocumentTypes.TIP,
            LeVADocumentUseCase.DocumentTypes.TIR,
            LeVADocumentUseCase.DocumentTypes.OVERALL_TIR
        ]
    ]

    // Document types per pipeline phase with an optional lifecycle constraint
    private static Map PIPELINE_PHASES = [
        (MROPipelineUtil.PipelinePhases.INIT): [
            (LeVADocumentUseCase.DocumentTypes.CS): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END,
            (LeVADocumentUseCase.DocumentTypes.DSD): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END,
            (LeVADocumentUseCase.DocumentTypes.FS): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END,
            (LeVADocumentUseCase.DocumentTypes.URS): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END
        ],
        (MROPipelineUtil.PipelinePhases.BUILD): [
            (LeVADocumentUseCase.DocumentTypes.DTP): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START,
            (LeVADocumentUseCase.DocumentTypes.DTR): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.SCP): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START,
            (LeVADocumentUseCase.DocumentTypes.SCR): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.SDS): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.OVERALL_DTR): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END,
            (LeVADocumentUseCase.DocumentTypes.OVERALL_SDS): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END
        ],
        (MROPipelineUtil.PipelinePhases.DEPLOY): [
            (LeVADocumentUseCase.DocumentTypes.TIP): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START,
            (LeVADocumentUseCase.DocumentTypes.TIR): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO
        ],
        (MROPipelineUtil.PipelinePhases.TEST): [
            (LeVADocumentUseCase.DocumentTypes.IVP): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START,
            (LeVADocumentUseCase.DocumentTypes.IVR): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.FTP): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_START,
            (LeVADocumentUseCase.DocumentTypes.FTR): MROPipelineUtil.PipelinePhaseLifecycleStage.POST_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.SCR): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_EXECUTE_REPO,
            (LeVADocumentUseCase.DocumentTypes.OVERALL_SCR): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END
        ],
        (MROPipelineUtil.PipelinePhases.RELEASE): [
        ],
        (MROPipelineUtil.PipelinePhases.FINALIZE): [
            (LeVADocumentUseCase.DocumentTypes.OVERALL_TIR): MROPipelineUtil.PipelinePhaseLifecycleStage.PRE_END
        ]
    ]

    // Document types per repository type with an optional phase constraint
    private static Map REPSITORY_TYPES = [
        (MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE): [
            (LeVADocumentUseCase.DocumentTypes.DTR): null,
            (LeVADocumentUseCase.DocumentTypes.FTR): null,
            (LeVADocumentUseCase.DocumentTypes.IVR): null,
            (LeVADocumentUseCase.DocumentTypes.SCR): MROPipelineUtil.PipelinePhases.BUILD,
            (LeVADocumentUseCase.DocumentTypes.SDS): null,
            (LeVADocumentUseCase.DocumentTypes.TIR): null
        ],
        (MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_SERVICE): [
            (LeVADocumentUseCase.DocumentTypes.IVR): null,
            (LeVADocumentUseCase.DocumentTypes.TIR): null
        ],
        (MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_TEST): [
            (LeVADocumentUseCase.DocumentTypes.SCR): MROPipelineUtil.PipelinePhases.TEST,
            (LeVADocumentUseCase.DocumentTypes.SDS): null
        ]
    ]

    LeVADocumentScheduler(IPipelineSteps steps, LeVADocumentUseCase usecase) {
        super(steps, usecase)
    }

    private boolean isDocumentApplicableForGampCategory(String documentType, String gampCategory) {
        return this.GAMP_CATEGORIES[gampCategory].contains(documentType)
    }

    private boolean isDocumentApplicableForPipelinePhaseAndLifecycleStage(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage) {
        def documentTypesForPipelinePhase = this.PIPELINE_PHASES[phase]
        if (!documentTypesForPipelinePhase) {
            return false
        }

        def result = documentTypesForPipelinePhase.containsKey(documentType)

        // Check if the document type defines a lifecycle stage constraint
        def lifecycleStageConstraintForDocumentType = documentTypesForPipelinePhase[documentType]
        if (lifecycleStageConstraintForDocumentType != null) {
            result = result && lifecycleStageConstraintForDocumentType == stage
        }

        return result
    }

    private boolean isDocumentApplicableForProject(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project) {
        def levaDocsCapability = project.capabilities.find { it instanceof Map && it.containsKey("LeVADocs") }?.LeVADocs
        if (!levaDocsCapability) {
            return false
        }

        def gampCategory = levaDocsCapability.GAMPCategory.toString()
        if (!gampCategory) {
            return false
        }

        if (!this.GAMP_CATEGORIES.keySet().contains(gampCategory)) {
            throw new IllegalArgumentException("Error: unable to assert applicability of document type '${documentType}' for project '${project.id}' in phase '${phase}'. The GAMP category '${gampCategory}' is not supported.")
        }

        def result = isDocumentApplicableForGampCategory(documentType, gampCategory) && isDocumentApplicableForPipelinePhaseAndLifecycleStage(documentType, phase, stage) && isRepositoryLevelDocument(documentType)
        // Applicable for certain document types only if the Jira service is configured in the release manager configuration
        if ([LeVADocumentUseCase.DocumentTypes.CS, LeVADocumentUseCase.DocumentTypes.DSD, LeVADocumentUseCase.DocumentTypes.FS, LeVADocumentUseCase.DocumentTypes.URS].contains(documentType)) {
            result = result && project.services?.jira != null
        }

        return result
    }

    private boolean isDocumentApplicableForRepo(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project, Map repo) {
        def levaDocsCapability = project.capabilities.find { it instanceof Map && it.containsKey("LeVADocs") }?.LeVADocs
        if (!levaDocsCapability) {
            return false
        }

        def gampCategory = levaDocsCapability.GAMPCategory.toString()
        if (!gampCategory) {
            return false
        }

        if (!this.GAMP_CATEGORIES.keySet().contains(gampCategory)) {
            throw new IllegalArgumentException("Error: unable to assert applicability of document type '${documentType}' for project '${project.id}' and repo '${repo.id}' in phase '${phase}'. The GAMP category '${gampCategory}' is not supported.")
        }

        return isDocumentApplicableForGampCategory(documentType, gampCategory) && isDocumentApplicableForPipelinePhaseAndLifecycleStage(documentType, phase, stage) && isDocumentApplicableForRepoTypeAndPhase(documentType, phase, repo)
    }

    private boolean isDocumentApplicableForRepoTypeAndPhase(String documentType, String phase, Map repo) {
        def documentTypesForRepoType = this.REPSITORY_TYPES[(repo.type.toLowerCase())]
        if (!documentTypesForRepoType) {
            return false
        }

        def result = documentTypesForRepoType.containsKey(documentType)

        // Check if the document type defines a phase constraint
        def phaseConstraintForDocumentType = documentTypesForRepoType[documentType]
        if (phaseConstraintForDocumentType != null) {
            result = result && phaseConstraintForDocumentType == phase
        }

        return result
    }

    private boolean isRepositoryLevelDocument(String documentType) {
        return !this.REPSITORY_TYPES.values().collect { it.keySet() }.flatten().contains(documentType)
    }

    protected boolean isDocumentApplicable(String documentType, String phase, MROPipelineUtil.PipelinePhaseLifecycleStage stage, Map project, Map repo = null) {
        return !repo
          ? isDocumentApplicableForProject(documentType, phase, stage, project)
          : isDocumentApplicableForRepo(documentType, phase, stage, project, repo)
    }
}
