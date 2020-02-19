package org.ods.usecase

import groovy.json.JsonOutput

import java.time.LocalDateTime

import org.apache.commons.io.FilenameUtils
import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.DocGenService
import org.ods.service.JenkinsService
import org.ods.service.LeVADocumentChaptersFileService
import org.ods.service.NexusService
import org.ods.service.OpenShiftService
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil
import org.ods.util.PDFUtil
import org.ods.util.PipelineUtil
import org.ods.util.Project
import org.ods.util.SortUtil

class LeVADocumentUseCase extends DocGenUseCase {

    class IssueTypes {
        static final String LEVA_DOCUMENTATION = "LeVA Documentation"
    }

    enum DocumentType {
        CS,
        DSD,
        DTP,
        DTR,
        FS,
        FTP,
        FTR,
        IVP,
        IVR,
        SCP,
        SCR,
        SDS,
        TIP,
        TIR,
        URS,
        OVERALL_DTR,
        OVERALL_IVR,
        OVERALL_SCR,
        OVERALL_SDS,
        OVERALL_TIR
    }

    private static Map DOCUMENT_TYPE_NAMES = [
        (DocumentType.CS as String): "Configuration Specification",
        (DocumentType.DSD as String): "System Design Specification",
        (DocumentType.DTP as String): "Software Development Testing Plan",
        (DocumentType.DTR as String): "Software Development Testing Report",
        (DocumentType.FS as String): "Functional Specification",
        (DocumentType.FTP as String): "Functional and Requirements Testing Plan",
        (DocumentType.FTR as String): "Functional and Requirements Testing Report",
        (DocumentType.IVP as String): "Configuration and Installation Testing Plan",
        (DocumentType.IVR as String): "Configuration and Installation Testing Report",
        (DocumentType.SCP as String): "Software Development (Coding and Code Review) Plan",
        (DocumentType.SCR as String): "Software Development (Coding and Code Review) Report",
        (DocumentType.SDS as String): "Software Design Specification",
        (DocumentType.TIP as String): "Technical Installation Plan",
        (DocumentType.TIR as String): "Technical Installation Report",
        (DocumentType.URS as String): "User Requirements Specification",
        (DocumentType.OVERALL_DTR as String): "Overall Software Development Testing Report",
        (DocumentType.OVERALL_IVR as String): "Overall Configuration and Installation Testing Report",
        (DocumentType.OVERALL_SCR as String): "Overall Software Development (Coding and Code Review) Report",
        (DocumentType.OVERALL_SDS as String): "Overall Software Design Specification",
        (DocumentType.OVERALL_TIR as String): "Overall Technical Installation Report"
    ]

    private static String DEVELOPER_PREVIEW_WATERMARK = "Developer Preview"

    private JenkinsService jenkins
    private JiraUseCase jiraUseCase
    private LeVADocumentChaptersFileService levaFiles
    private OpenShiftService os
    private SonarQubeUseCase sq

    LeVADocumentUseCase(Project project, IPipelineSteps steps, MROPipelineUtil util, DocGenService docGen, JenkinsService jenkins, JiraUseCase jiraUseCase, LeVADocumentChaptersFileService levaFiles, NexusService nexus, OpenShiftService os, PDFUtil pdf, SonarQubeUseCase sq) {
        super(project, steps, util, docGen, nexus, pdf)
        this.jenkins = jenkins
        this.jiraUseCase = jiraUseCase
        this.levaFiles = levaFiles
        this.os = os
        this.sq = sq
    }

    protected Map computeComponentMetadata(String documentType) {
        return this.project.components.collectEntries { component ->
            def normComponentName = component.name.replaceAll("Technology-", "")

            def repo_ = this.project.repositories.find { [it.id, it.name].contains(normComponentName) }
            if (!repo_) {
                throw new RuntimeException("Error: unable to create ${documentType}. Could not find a repository configuration with id or name equal to '${normComponentName}' for Jira component '${component.name}' in project '${this.project.key}'.")
            }

            def metadata = repo_.metadata

            return [
                component.name,
                [
                    componentId: metadata.id ?: "N/A - part of this application",
                    componentType: (repo_.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE) ? "ODS Component" : "Software",
                    description: metadata.description,
                    nameOfSoftware: metadata.name,
                    references: metadata.references ?: "N/A",
                    supplier: metadata.supplier,
                    version: metadata.version
                ]
            ]
        }
    }

    // FIXME: re-implement to depend on testResults, not on testIssues
    // since we want to report all executed tests (and draw conclusions from them)
    protected Map computeTestDiscrepancies(String name, List testIssues) {
        def result = [
            discrepancies: "No discrepancies found.",
            conclusion: [
                summary: "Complete success, no discrepancies",
                statement: "It is determined that all steps of the ${name} have been successfully executed and signature of this report verifies that the tests have been performed according to the plan. No discrepancies occurred."
            ]
        ]

        def failed = []
        def missing = []

        testIssues.each { issue ->
            if (!issue.test.isSuccess && !issue.test.isMissing) {
                failed << issue.key
            }

            if (!issue.test.isSuccess && issue.test.isMissing) {
                missing << issue.key
            }
        }

        if (failed.isEmpty() && !missing.isEmpty()) {
            result.discrepancies = "The following minor discrepancies were found during testing: ${missing.join(", ")}."

            result.conclusion = [
               summary: "Success - minor discrepancies found",
               statement: "Some discrepancies were found as tests were not executed, this may be per design."
            ]
        } else if (!failed.isEmpty()) {
            if (!missing.isEmpty()) {
                failed.addAll(missing)
                failed.sort()
            }

            result.discrepancies = "The following major discrepancies were found during testing: ${(failed).join(", ")}."

            result.conclusion = [
               summary: "No success - major discrepancies found",
               statement: "Some discrepancies occured as tests did fail. It is not recommended to continue!"
            ]
        }

        return result
    }

    String createCS(Map repo = null, Map data = null) {
        def documentType = DocumentType.CS as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def interfaces = this.project.getSystemRequirementsTypeInterfaces().collect { req ->
            [
                key: req.configSpec.key,
                // TODO: change ur_key to req_key in template
                req_key: req.key,
                description: req.description
            ]
        }

        if (!sections."sec4") sections."sec4" = [:]
        sections."sec4".items = SortUtil.sortIssuesByProperties(interfaces, ["req_key", "key"])

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createDSD(Map repo = null, Map data = null) {
        def documentType = DocumentType.DSD as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def componentMetadata = this.computeComponentMetadata(documentType)

        def systemDesignSpecifications = this.project.getTechnicalSpecifications().collect { techSpec ->
            [
                key: techSpec.key,
                // TODO: change ur_key to req_key and make column content-wrappable in template
                req_key: techSpec.requirements.collect{ it }.join(", "),
                description: techSpec.systemDesignSpec,
                // TODO: prefix properties in sec5s1 with .metadata in template 
                metadata: techSpec.components.collect { componentName ->
                    return componentMetadata[componentName]
                }.join(", ")
            ]
        }

        if (!sections."sec3") sections."sec3" = [:]
        sections."sec3".specifications = SortUtil.sortIssuesByProperties(systemDesignSpecifications, ["req_key", "key"])

        if (!sections."sec5s1") sections."sec5s1" = [:]
        sections."sec5s1".specifications = SortUtil.sortIssuesByProperties(systemDesignSpecifications, ["key"])

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createDTP(Map repo = null, Map data = null) {
        def documentType = DocumentType.DTP as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                // TODO: change template from this.project.repositories to repositories
                repositories: this.project.repositories,
                sections: sections,
                tests: this.project.getAutomatedTestsTypeUnit().collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            description: testIssue.description ?: "",
                            // TODO: change template from isRelatedTo to systemRequirement
                            systemRequirement: testIssue.requirements.join(", ")
                        ]
                    ]
                }
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createDTR(Map repo, Map data) {
        def documentType = DocumentType.DTR as String

        data = data.tests.unit

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def testIssues = this.project.getAutomatedTestsTypeUnit("Technology-${repo.id}")

        def matchedHandler = { result ->
            result.each { testIssue, testCase ->
                testIssue.isSuccess = !(testCase.error || testCase.failure || testCase.skipped)
                testIssue.isMissing = false
            }
        }

        def unmatchedHandler = { result ->
            result.each { testIssue ->
                testIssue.isSuccess = false
                testIssue.isMissing = true
            }
        }

        this.jiraUseCase.matchTestIssuesAgainstTestResults(testIssues, data?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def discrepancies = this.computeTestDiscrepancies("Development Tests", testIssues)

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            data: [
                repo: repo,
                sections: sections,
                tests: testIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            description: testIssue.description ?: "",
                            // TODO: change template from isRelatedTo to systemRequirement
                            systemRequirement: testIssue.requirements.join(", "),
                            success: testIssue.isSuccess ? "Y" : "N",
                            remarks: testIssue.isMissing ? "not executed" : ""
                        ]
                    ]
                },
                testfiles: data.testReportFiles.collect { file ->
                    [ name: file.getName(), path: file.getPath() ]
                },
                testsuites: data.testResults,
                discrepancies: discrepancies.discrepancies,
                conclusion: [
                    summary: discrepancies.conclusion.summary,
                    statement : discrepancies.conclusion.statement
                ]
            ]
        ]

        def files = data.testReportFiles.collectEntries { file ->
            [ "raw/${file.getName()}", file.getBytes() ]
        }

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        def uri = this.createDocument(documentType, repo, data_, files, modifier, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createFS(Map repo = null, Map data = null) {
        def documentType = DocumentType.FS as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // TODO: create full re-implementation
        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createFTP(Map repo = null, Map data = null) {
        def documentType = DocumentType.FTP as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def acceptanceTestIssues = this.project.getAutomatedTestsTypeAcceptance()
        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections,
                acceptanceTests: acceptanceTestIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            description: testIssue.description ?: "",
                            ur_key: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                            risk_key: tetsIssue.risks ? testIssue.risks.join(", ") : "N/A"
                        ]
                    ]
                },
                integrationTests: integrationTestIssues.collectEntries { testIssue ->
                    [
                        issue.key,
                        [
                            key: testIssue.key,
                            description: testIssue.description ?: "",
                            ur_key: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                            risk_key: tetsIssue.risks ? testIssue.risks.join(", ") : "N/A"
                        ]
                    ]
                }
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createFTR(Map repo, Map data) {
        def documentType = DocumentType.FTR as String

        def acceptanceTestData = data.tests.acceptance
        def integrationTestData = data.tests.integration

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def matchedHandler = { result ->
            result.each { testIssue, testCase ->
                testIssue.isSuccess = !(testCase.error || testCase.failure || testCase.skipped)
                testIssue.isMissing = false
                testIssue.timestamp = testCase.timestamp
            }
        }

        def unmatchedHandler = { result ->
            result.each { testIssue ->
                testIssue.isSuccess = false
                testIssue.isMissing = true
            }
        }

        def acceptanceTestIssues = this.project.getAutomatedTestsTypeAcceptance()
        this.jiraUseCase.matchTestIssuesAgainstTestResults(acceptanceTestIssues, acceptanceTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()
        this.jiraUseCase.matchTestIssuesAgainstTestResults(integrationTestIssues, integrationTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def discrepancies = this.computeTestDiscrepancies("Functional and Requirements Tests", (acceptanceTestIssues + integrationTestIssues))

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections,
                acceptanceTests: acceptanceTestIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            datetime: testIssue.timestamp ? testIssue.timestamp.replaceAll("T", "</br>") : "N/A",
                            description: testIssue.description ?: "",
                            remarks: testIssue.isMissing ? "not executed" : "",
                            risk_key: tetsIssue.risks ? testIssue.risks.join(", ") : "N/A",
                            success: testIssue.sSuccess ? "Y" : "N",
                            ur_key: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A"
                        ]
                    ]
                },
                integrationTests: integrationTestIssues.collectEntries { issue ->
                    [
                        issue.key,
                        [
                            key: testIssue.key,
                            datetime: testIssue.timestamp ? testIssue.timestamp.replaceAll("T", "</br>") : "N/A",
                            description: testIssue.description ?: "",
                            remarks: testIssue.isMissing ? "not executed" : "",
                            risk_key: tetsIssue.risks ? testIssue.risks.join(", ") : "N/A",
                            success: testIssue.sSuccess ? "Y" : "N",
                            ur_key: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A"
                        ]
                    ]
                },
                testfiles: (acceptanceTestData + integrationTestData).testReportFiles.collect { file ->
                    [ name: file.getName(), path: file.getPath() ]
                },
                conclusion: [
                    summary: discrepancies.conclusion.summary,
                    statement : discrepancies.conclusion.statement
                ]
            ]
        ]

        def files = (acceptanceTestData + integrationTestData).testReportFiles.collectEntries { file ->
            [ "raw/${file.getName()}", file.getBytes() ]
        }

        def uri = this.createDocument(documentType, null, data_, files, null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createIVP(Map repo = null, Map data = null) {
        def documentType = DocumentType.IVP as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def data_ = [
            metadata: this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                // TODO: change data.project.repositories to data.repositories in template
                repositories: this.project.repositories,
                sections: sections,
                tests: this.project.getAutomatedTestsTypeInstallation().collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            summary: testIssue.name,
                            // TODO: change template from isRelatedTo to techSpec
                            techSpec: testIssue.techSpecs.join(", ")
                        ]
                    ]
                }
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createIVR(Map repo, Map data) {
        def documentType = DocumentType.IVR as String

        data = data.tests.installation

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def testIssues = this.project.getAutomatedTestsTypeInstallation()

        def matchedHandler = { result ->
            result.each { testIssue, testCase ->
                testIssue.isSuccess = !(testCase.error || testCase.failure || testCase.skipped)
                testIssue.isMissing = false
            }
        }

        def unmatchedHandler = { result ->
            result.each { testIssue ->
                testIssue.isSuccess = false
                testIssue.isMissing = true
            }
        }

        this.jiraUseCase.matchTestIssuesAgainstTestResults(testIssues, data?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                // TODO: change data.project.repositories to data.repositories in template
                repositories: this.project.repositories,
                sections: sections,
                tests: testIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key: testIssue.key,
                            description: testIssue.description ?: "",
                            remarks: testIssue.isMissing ? "not executed" : "",
                            success: testIssue.isSuccess ? "Y" : "N",
                            summary: testIssue.name,
                            // TODO: change template from isRelatedTo to techSpec
                            techSpec: testIssue.techSpecs.join(", ")
                        ]
                    ]
                },
                testfiles: data.testReportFiles.collect { file ->
                    [ name: file.getName(), path: file.getPath() ]
                }
            ]
        ]

        def files = data.testReportFiles.collectEntries { file ->
            [ "raw/${file.getName()}", file.getBytes() ]
        }

        def uri = this.createDocument(documentType, null, data_, files, null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createSCP(Map repo = null, Map data = null) {
        def documentType = DocumentType.SCP as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                // TODO: change data.project.repositories to data.repositories in template
                repositories: this.project.repositories,
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createSCR(Map repo, Map data = null) {
        def documentType = DocumentType.SCR as String

        def sqReportsPath = "${PipelineUtil.SONARQUBE_BASE_DIR}/${repo.id}"
        def sqReportsStashName = "scrr-report-${repo.id}-${this.steps.env.BUILD_ID}"

        // Unstash SonarQube reports into path
        def hasStashedSonarQubeReports = this.jenkins.unstashFilesIntoPath(sqReportsStashName, "${this.steps.env.WORKSPACE}/${sqReportsPath}", "SonarQube Report")
        if (!hasStashedSonarQubeReports) {
            throw new RuntimeException("Error: unable to unstash SonarQube reports for repo '${repo.id}' from stash '${sqReportsStashName}'.")
        }

        // Load SonarQube report files from path
        def sqReportFiles = this.sq.loadReportsFromPath("${this.steps.env.WORKSPACE}/${sqReportsPath}")
        if (sqReportFiles.isEmpty()) {
            throw new RuntimeException("Error: unable to load SonarQube reports for repo '${repo.id}' from path '${this.steps.env.WORKSPACE}/${sqReportsPath}'.")
        }

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            data: [
                sections: sections
            ]
        ]

        def files = [:]
        /*
        // TODO: conversion of a SonarQube report results in an ambiguous NPE.
        // Research did not reveal any meaningful results. Further, Apache POI
        // depends on Commons Compress, but unfortunately Jenkins puts an older
        // version onto the classpath which results in an error. Therefore, iff
        // the NPE can be fixed, this code would need to run outside of Jenkins,
        // such as the DocGen service.

        def sonarQubePDFDoc = this.pdf.convertFromWordDoc(sonarQubeWordDoc)
        modifier = { document ->
            // Merge the current document with the SonarQube report
            return this.pdf.merge([ document, sonarQubePDFDoc ])
        }

        // As our plan B below, we instead add the SonarQube report into the
        // SCR's .zip archive.
        */
        def name = this.getDocumentBasename("SCRR", this.project.buildParams.version, this.steps.env.BUILD_ID, repo)
        def sqReportFile = sqReportFiles.first()
        files << [ "${name}.${FilenameUtils.getExtension(sqReportFile.getName())}": sqReportFile.getBytes() ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, repo, data_, files, modifier, null, watermarkText)
    }

    String createSDS(Map repo, Map data = null) {
        def documentType = DocumentType.SDS as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            data: [
                repo: repo,
                sections: sections
            ]
        ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, repo, data_, [:], modifier, null, this.getWatermarkText(documentType))
    }

    String createTIP(Map repo = null, Map data = null) {
        def documentType = DocumentType.TIP as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                // TODO: change this.project.id to project_key in template
                project_key: this.project.key,
                // TODO: change data.project.repositories and data.repos to data.repositories in template
                repositories: this.project.repositories,
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createTIR(Map repo, Map data = null) {
        def documentType = DocumentType.TIR as String

        def pods = this.os.getPodDataForComponent(repo.id)

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            openShiftData: [
                ocpBuildId           : repo?.data.odsBuildArtifacts?."OCP Build Id" ?: "N/A",
                ocpDockerImage       : repo?.data.odsBuildArtifacts?."OCP Docker image" ?: "N/A",
                ocpDeploymentId      : repo?.data.odsBuildArtifacts?."OCP Deployment Id" ?: "N/A",
                podName              : pods?.items[0]?.metadata?.name ?: "N/A",
                podNamespace         : pods?.items[0]?.metadata?.namespace ?: "N/A",
                podCreationTimestamp : pods?.items[0]?.metadata?.creationTimestamp ?: "N/A",
                podEnvironment       : pods?.items[0]?.metadata?.labels?.env ?: "N/A",
                podNode              : pods?.items[0]?.spec?.nodeName ?: "N/A",
                podIp                : pods?.items[0]?.status?.podIP ?: "N/A",
                podStatus            : pods?.items[0]?.status?.phase ?: "N/A"
            ],
            data: [
                repo: repo,
                sections: sections
            ]
        ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, repo, data_, [:], modifier, null, watermarkText)
    }

    String createURS(Map repo = null, Map data = null) {
        def documentType = DocumentType.URS as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // TODO: complete re-implementation
        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data: [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createOverallDTR(Map repo = null, Map data = null) {
        def documentTypeName = DOCUMENT_TYPE_NAMES[DocumentType.OVERALL_DTR as String]
        def metadata = this.getDocumentMetadata(documentTypeName)

        def documentType = DocumentType.DTR as String

        def uri = this.createOverallDocument("Overall-Cover", documentType, metadata, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${documentTypeName} has been generated and is available at: ${uri}.")
        return uri
    }

    String createOverallSCR(Map repo = null, Map data = null) {
        def documentTypeName = DOCUMENT_TYPE_NAMES[DocumentType.OVERALL_SCR as String]
        def metadata = this.getDocumentMetadata(documentTypeName)

        def documentType = DocumentType.SCR as String

        def uri = this.createOverallDocument("Overall-Cover", documentType, metadata, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${documentTypeName} has been generated and is available at: ${uri}.")
        return uri
    }

    String createOverallSDS(Map repo = null, Map data = null) {
        def documentTypeName = DOCUMENT_TYPE_NAMES[DocumentType.OVERALL_SDS as String]
        def metadata = this.getDocumentMetadata(documentTypeName)

        def documentType = DocumentType.SDS as String

        def uri = this.createOverallDocument("Overall-Cover", documentType, metadata, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${documentTypeName} has been generated and is available at: ${uri}.")
        return uri
    }

    String createOverallTIR(Map repo = null, Map data = null) {
        def documentTypeName = DOCUMENT_TYPE_NAMES[DocumentType.OVERALL_TIR as String]
        def metadata = this.getDocumentMetadata(documentTypeName)

        def documentType = DocumentType.TIR as String

        def visitor = { data_ ->
            // Append another section for the Jenkins build log
            data_.sections << [
                heading: "Jenkins Build Log"
            ]

            // Add Jenkins build log data
            data_.jenkinsData = [
                log: this.jenkins.getCurrentBuildLogAsText()
            ]
        }

        def uri = this.createOverallDocument("Overall-TIR-Cover", documentType, metadata, visitor, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${documentTypeName} has been generated and is available at: ${uri}.")
        return uri
    }

    Map getDocumentMetadata(String documentTypeName, Map repo = null) {
        def name = this.project.name
        if (repo) {
            name += ": ${repo.id}"
        }

        def metadata = [
            id: null, // unused
            name: name,
            description: this.project.description,
            type: documentTypeName,
            version: this.steps.env.RELEASE_PARAM_VERSION,
            date_created: LocalDateTime.now().toString(),
            buildParameter: this.project.buildParams,
            git: repo ? repo.data.git : this.project.gitData,
            jenkins: [
                buildNumber: this.steps.env.BUILD_NUMBER,
                buildUrl: this.steps.env.BUILD_URL,
                jobName: this.steps.env.JOB_NAME
            ]
        ]

        metadata.header = ["${documentTypeName}, Config Item: ${metadata.buildParameter.configItem}", "Doc ID/Version: see auto-generated cover page"]

        return metadata
    }

    private String getJiraTrackingIssueLabelForDocumentType(String documentType) {
        def environment = this.project.buildParams.targetEnvironmentToken

        def label = LeVADocumentScheduler.ENVIRONMENT_TYPE[environment].get(documentType)
        if (!label && environment.equals('D')) {
            label = documentType
        }

        return "LeVA_Doc:${label}"
    }

    List<String> getSupportedDocuments() {
        return DocumentType.values().collect { it as String }
    }

    protected String getWatermarkText(String documentType) {
        def environment = this.project.buildParams.targetEnvironmentToken

        // The watermark only applies in DEV environment (for documents not to be delivered from that environment)
        if (environment.equals('D') && !LeVADocumentScheduler.ENVIRONMENT_TYPE['D'].containsKey(documentType)){
            return this.DEVELOPER_PREVIEW_WATERMARK
        }

        return null
    }

    void notifyJiraTrackingIssue(String documentType, String message) {
        if (!this.jiraUseCase) return
        if (!this.jiraUseCase.jira) return

        def jiraDocumentLabel = this.getJiraTrackingIssueLabelForDocumentType(documentType)

        def jqlQuery = [ jql: "project = ${project.key} AND issuetype = '${IssueTypes.LEVA_DOCUMENTATION}' AND labels = ${jiraDocumentLabel}" ]

        // Search for the Jira issue associated with the document
        def jiraIssues = this.jiraUseCase.jira.getIssuesForJQLQuery(jqlQuery)
        if (jiraIssues.size() != 1) {
            throw new RuntimeException("Error: Jira query returned ${jiraIssues.size()} issues: '${jqlQuery}'.")
        }

        // Add a comment to the Jira issue with a link to the report
        this.jiraUseCase.jira.appendCommentToIssue(jiraIssues.first().key, message)
    }
}
