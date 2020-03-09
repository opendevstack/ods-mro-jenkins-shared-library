package org.ods.usecase

import org.apache.commons.io.FilenameUtils
import org.ods.scheduler.LeVADocumentScheduler
import org.ods.service.*
import org.ods.util.*

import java.time.LocalDateTime

class LeVADocumentUseCase extends DocGenUseCase {

    class IssueTypes {
        static final String LEVA_DOCUMENTATION = "LeVA Documentation"
    }

    enum DocumentType {
        CSD,
        DIL,
        DTP,
        DTR,
        RA,
        CFTP,
        CFTR,
        IVP,
        IVR,
        SSDS,
        TCP,
        TCR,
        TIP,
        TIR,
        OVERALL_DTR,
        OVERALL_IVR,
        OVERALL_TIR
    }

    private static Map DOCUMENT_TYPE_NAMES = [
        (DocumentType.CSD as String)        : "Combined Specification Document",
        (DocumentType.DIL as String)        : "Discrepancy Log",
        (DocumentType.DTP as String)        : "Software Development Testing Plan",
        (DocumentType.DTR as String)        : "Software Development Testing Report",
        (DocumentType.CFTP as String)       : "Combined Functional and Requirements Testing Plan",
        (DocumentType.CFTR as String)       : "Combined Functional and Requirements Testing Report",
        (DocumentType.IVP as String)        : "Configuration and Installation Testing Plan",
        (DocumentType.IVR as String)        : "Configuration and Installation Testing Report",
        (DocumentType.RA as String)         : "Risk Assessment",
        (DocumentType.SSDS as String)       : "Software Design Specification",
        (DocumentType.TCP as String)        : "Test Case Plan",
        (DocumentType.TCR as String)        : "Test Case Report",
        (DocumentType.TIP as String)        : "Technical Installation Plan",
        (DocumentType.TIR as String)        : "Technical Installation Report",
        (DocumentType.OVERALL_DTR as String): "Overall Software Development Testing Report",
        (DocumentType.OVERALL_IVR as String): "Overall Configuration and Installation Testing Report",
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

    /**
     * This computes the information related to the components (modules) that are being developed
     * @param documentType
     * @return
     */
    protected Map computeComponentMetadata(String documentType) {
        return this.project.components.collectEntries { component ->
            def normComponentName = component.name.replaceAll("Technology-", "")
            def repo_ = this.project.repositories.find { [it.id, it.name, it.metadata.name].contains(normComponentName) }
            if (!repo_) {
                throw new RuntimeException("Error: unable to create ${documentType}. Could not find a repository configuration with id or name equal to '${normComponentName}' for Jira component '${component.name}' in project '${this.project.key}'.")
            }

            def metadata = repo_.metadata

            return [
                component.name,
                [
                    key               : component.key,
                    componentName     : component.name,
                    componentId       : metadata.id ?: "N/A - part of this application",
                    componentType     : (repo_.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE) ? "ODS Component" : "Software",
                    odsRepoType       : repo_.type?.toLowerCase(),
                    description       : metadata.description,
                    nameOfSoftware    : metadata.name,
                    references        : metadata.references ?: "N/A",
                    supplier          : metadata.supplier,
                    version           : metadata.version,
                    requirements      : component.getResolvedSystemRequirements(),
                    softwareDesignSpec: component.getResolvedTechnicalSpecifications().findAll {
                        it.softwareDesignSpec
                    }.collect {
                        [key: it.key, softwareDesignSpec: it.softwareDesignSpec]
                    }
                ]
            ]
        }
    }

    private Map obtainCodeReviewReport(List<Map> repos) {
        return repos.collectEntries { r ->
            def sqReportsPath = "${PipelineUtil.SONARQUBE_BASE_DIR}/${r.id}"
            def sqReportsStashName = "scrr-report-${r.id}-${this.steps.env.BUILD_ID}"

            // Unstash SonarQube reports into path
            def hasStashedSonarQubeReports = this.jenkins.unstashFilesIntoPath(sqReportsStashName, "${this.steps.env.WORKSPACE}/${sqReportsPath}", "SonarQube Report")
            if (!hasStashedSonarQubeReports) {
                throw new RuntimeException("Error: unable to unstash SonarQube reports for repo '${r.id}' from stash '${sqReportsStashName}'.")
            }

            // Load SonarQube report files from path
            def sqReportFiles = this.sq.loadReportsFromPath("${this.steps.env.WORKSPACE}/${sqReportsPath}")
            if (sqReportFiles.isEmpty()) {
                throw new RuntimeException("Error: unable to load SonarQube reports for repo '${r.id}' from path '${this.steps.env.WORKSPACE}/${sqReportsPath}'.")
            }

            def name = this.getDocumentBasename("SCRR", this.project.buildParams.version, this.steps.env.BUILD_ID, r)
            def sqReportFile = sqReportFiles.first()

            // TODO transform sq reports to PDF
            // This is not possible right now due to what it seems to be an issue with the documents themselves,
            // where the tables are malformed (it says that we have 1 column but more are in the rows)
            // See the PDFUtils method for more details
            //def sonarQubePDFDoc = this.pdf.convertFromWordDoc(sqReportFile)
            // Plan B is to add the docX files to the SSDS' zip file
            return ["${name}.${FilenameUtils.getExtension(sqReportFile.getName())}", sqReportFile.getBytes()]
        }
    }

    // FIXME: re-implement to depend on testResults, not on testIssues
    // since we want to report all executed tests (and draw conclusions from them)
    protected Map computeTestDiscrepancies(String name, List jiraTestIssues) {
        def result = [
            discrepancies: "No discrepancies found.",
            conclusion   : [
                summary  : "Complete success, no discrepancies",
                statement: "It is determined that all steps of the ${name} have been successfully executed and signature of this report verifies that the tests have been performed according to the plan. No discrepancies occurred."
            ]
        ]

        def failed = []
        def missing = []

        jiraTestIssues.each { issue ->
            if (!issue.isSuccess && !issue.isMissing) {
                failed << issue.key
            }

            if (!issue.isSuccess && issue.isMissing) {
                missing << issue.key
            }
        }

        if (failed.isEmpty() && !missing.isEmpty()) {
            result.discrepancies = "The following minor discrepancies were found during testing: ${missing.join(", ")}."

            result.conclusion = [
                summary  : "Success - minor discrepancies found",
                statement: "Some discrepancies were found as tests were not executed, this may be per design."
            ]
        } else if (!failed.isEmpty()) {
            if (!missing.isEmpty()) {
                failed.addAll(missing)
                failed.sort()
            }

            result.discrepancies = "The following major discrepancies were found during testing: ${(failed).join(", ")}."

            result.conclusion = [
                summary  : "No success - major discrepancies found",
                statement: "Some discrepancies occured as tests did fail. It is not recommended to continue!"
            ]
        }

        return result
    }

    String createCSD(Map repo = null, Map data = null) {
        def documentType = DocumentType.CSD as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def requirements = this.project.getSystemRequirements()
            .groupBy { it.gampTopic.toLowerCase() }.collectEntries { k, v ->
            [
                k.replaceAll(" ", "").toLowerCase(),
                SortUtil.sortIssuesByProperties(v.collect { req ->
                    [
                        key          : req.key,
                        applicability: "Mandatory",
                        ursName      : req.name,
                        csName       : req.configSpec.name,
                        fsName       : req.funcSpec.name
                    ]
                }, ["key"])
            ]
        }
        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections    : sections,
                requirements: requirements
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createDIL(Map repo = null, Map data = null) {
        def documentType = DocumentType.DIL as String

        def watermarkText = this.getWatermarkText(documentType)

        def bugs = this.project.getBugs().each { bug ->
            bug.tests = bug.getResolvedTests()
        }

        def acceptanceTestBugs = bugs.findAll {
            bug ->
                bug.tests.findAll {
                    test -> test.testType == Project.TestType.ACCEPTANCE
                }
        }

        def integrationTestBugs = bugs.findAll {
            bug ->
                bug.tests.findAll {
                    test -> test.testType == Project.TestType.INTEGRATION
                }
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [:]
        ]

        if (!integrationTestBugs.isEmpty()) {
            data_.data.integrationTests = integrationTestBugs.collectEntries { bug ->
                [
                    bug.key,
                    [
                        //Discrepancy ID -> BUG Issue ID
                        discrepancyID        : bug.key,
                        //Test Case No. -> JIRA (Test Case Key)
                        testcaseID           : bug.tests.first().key,
                        //-	Level of Test Case = Unit / Integration / Acceptance / Installation
                        level                : "Integration",
                        //Description of Failure or Discrepancy -> Bug Issue Summary
                        description          : bug.name,
                        //Remediation Action -> "To be fixed"
                        remediation          : "To be fixed",
                        //Responsible / Due Date -> JIRA (assignee, Due date)
                        responsibleAndDueDate: "${bug.assignee ? bug.assignee : 'N/A'} / ${bug.dueDate ? bug.dueDate : 'N/A'}",
                        //Outcome of the Resolution -> Bug Status
                        outcomeResolution    : bug.status,
                        //Resolved Y/N -> JIRA Status -> Done = Yes
                        resolved             : bug.status == "Done" ? "Yes" : "No"
                    ]
                ]
            }
        }

        if (!acceptanceTestBugs.isEmpty()) {
            data_.data.acceptanceTests = acceptanceTestBugs.collectEntries { bug ->
                [
                    bug.key,
                    [
                        //Discrepancy ID -> BUG Issue ID
                        discrepancyID        : bug.key,
                        //Test Case No. -> JIRA (Test Case Key)
                        testcaseID           : bug.tests.first().key,
                        //-	Level of Test Case = Unit / Integration / Acceptance / Installation
                        level                : "Acceptance",
                        //Description of Failure or Discrepancy -> Bug Issue Summary
                        description          : bug.name,
                        //Remediation Action -> "To be fixed"
                        remediation          : "To be fixed",
                        //Responsible / Due Date -> JIRA (assignee, Due date)
                        responsibleAndDueDate: "${bug.assignee ? bug.assignee : 'N/A'} / ${bug.dueDate ? bug.dueDate : 'N/A'}",
                        //Outcome of the Resolution -> Bug Status
                        outcomeResolution    : bug.status,
                        //Resolved Y/N -> JIRA Status -> Done = Yes
                        resolved             : bug.status == "Done" ? "Yes" : "No"
                    ]
                ]
            }
        }

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
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
            data    : [
                repositories: this.project.repositories.collect { repo_ ->
                    [
                        id         : repo_.id,
                        description: repo_.metadata.description,
                        url        : repo_.url
                    ]
                },
                sections    : sections,
                tests       : this.project.getAutomatedTestsTypeUnit().collectEntries { testIssue ->
                    def techSpecsWithSoftwareDesignSpec = testIssue.getTechnicalSpecifications().findAll { it.softwareDesignSpec }.collect { it.key }

                    [
                        testIssue.key,
                        [
                            key               : testIssue.key,
                            description       : testIssue.description ?: "",
                            systemRequirement : testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                            softwareDesignSpec: techSpecsWithSoftwareDesignSpec ? techSpecsWithSoftwareDesignSpec.join(", ") : "N/A"
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
            data    : [
                repo         : repo,
                sections     : sections,
                tests        : testIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key               : testIssue.key,
                            description       : testIssue.description ?: "",
                            // TODO: change template from isRelatedTo to systemRequirement
                            systemRequirement : testIssue.requirements.join(", "),
                            success           : testIssue.isSuccess ? "Y" : "N",
                            remarks           : testIssue.isMissing ? "not executed" : "",
                            softwareDesignSpec: testIssue.getTechnicalSpecifications().findAll { it.softwareDesignSpec } ?
                                testIssue.getTechnicalSpecifications().findAll { it.softwareDesignSpec }.collect { it.key }.join(", ") : "N/A"
                        ]
                    ]
                },
                testfiles    : data.testReportFiles.collect { file ->
                    [name: file.getName(), path: file.getPath()]
                },
                testsuites   : data.testResults,
                discrepancies: discrepancies.discrepancies,
                conclusion   : [
                    summary  : discrepancies.conclusion.summary,
                    statement: discrepancies.conclusion.statement
                ]
            ]
        ]

        def files = data.testReportFiles.collectEntries { file ->
            ["raw/${file.getName()}", file.getBytes()]
        }

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        def uri = this.createDocument(documentType, repo, data_, files, modifier, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createCFTP(Map repo = null, Map data = null) {
        def documentType = DocumentType.CFTP as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def acceptanceTestIssues = this.project.getAutomatedTestsTypeAcceptance()
        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections        : sections,
                acceptanceTests : acceptanceTestIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key        : testIssue.key,
                            description: testIssue.description ?: "",
                            ur_key     : testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                            risk_key   : testIssue.risks ? testIssue.risks.join(", ") : "N/A"
                        ]
                    ]
                },
                integrationTests: integrationTestIssues.collectEntries { testIssue ->
                    [
                        testIssue.key,
                        [
                            key        : testIssue.key,
                            description: testIssue.description ?: "",
                            ur_key     : testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                            risk_key   : testIssue.risks ? testIssue.risks.join(", ") : "N/A"
                        ]
                    ]
                }
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createRA(Map repo = null, Map data = null) {
        def documentType = DocumentType.RA as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def obtainEnumShort = { category, value -> this.project.getEnumDictionary(category)[value as String]."short" }
        def obtainEnumValue = { category, value -> this.project.getEnumDictionary(category)[value as String].value }

        def risks = this.project.getRisks().collect { r ->
            def mitigationsText = r.mitigations ? r.mitigations.join(", ") : "None"
            def testsText = r.tests ? r.tests.join(", ") : "None"
            r.proposedMeasures = "Mitigations: ${mitigationsText}<br/>Tests: ${testsText}"

            def requirements = r.getResolvedSystemRequirements()
            r.requirements = requirements.collect { it.name }.join("<br/>")
            r.requirementsKey = requirements.collect { it.key }.join("<br/>")

            r.gxpRelevance = (obtainEnumShort("gxprelevance", r.gxpRelevance).contains("N")) ? "No" : "Yes"
            r.probabilityOfOccurrence = obtainEnumShort("ProbabilityOfOccurrence", r.probabilityOfOccurrence)
            r.severityOfImpact = obtainEnumValue("SeverityOfImpact", r.severityOfImpact)
            r.probabilityOfDetection = obtainEnumShort("ProbabilityOfDetection", r.probabilityOfDetection)
            r.riskPriority = obtainEnumValue("RiskPriority", r.riskPriority)

            return r
        }

        def proposedMeasuresDesription = this.project.getRisks().collect { r ->
            (r.getResolvedTests().collect {
                if (!it) throw new IllegalArgumentException("Error: test for requirement ${r.key} could not be obtained. Check if all of ${r.tests.join(", ")} exist in JIRA")
                [key: it.key, name: it.name, type: "test", referencesRisk: r.key]
            } +
                r.getResolvedMitigations().collect { [key: it?.key, name: it?.name, type: "mitigation", referencesRisk: r.key] })
        }.flatten()

        if (!sections."sec4s2s2") sections."sec4s2s2" = [:]

        if (this.project.getProjectProperties()."PROJECT.USES_POO" == "true") {
            sections."sec4s2s2" = [
                usesPoo          : "true",
                lowDescription   : this.project.getProjectProperties()."PROJECT.POO_CAT.LOW",
                mediumDescription: this.project.getProjectProperties()."PROJECT.POO_CAT.MEDIUM",
                highDescription  : this.project.getProjectProperties()."PROJECT.POO_CAT.HIGH"
            ]
        }

        if (!sections."sec5") sections."sec5" = [:]
        sections."sec5".risks = SortUtil.sortIssuesByProperties(risks, ["key"])
        sections."sec5".proposedMeasures = SortUtil.sortIssuesByProperties(proposedMeasuresDesription, ["key"])

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createCFTR(Map repo, Map data) {
        def documentType = DocumentType.CFTR as String

        def acceptanceTestData = data.tests.acceptance
        def integrationTestData = data.tests.integration

        // Obtain the total number of test in report
        def numberAcceptanceTest = getNumberOfTest(acceptanceTestData.testResults)
        def numberIntegrationTest = getNumberOfTest(integrationTestData.testResults)

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
        acceptanceTestIssues = SortUtil.sortIssuesByProperties(acceptanceTestIssues ?: [], ["key"])

        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()
        this.jiraUseCase.matchTestIssuesAgainstTestResults(integrationTestIssues, integrationTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)
        integrationTestIssues = SortUtil.sortIssuesByProperties(integrationTestIssues ?: [], ["key"])

        def discrepancies = this.computeTestDiscrepancies("Functional and Requirements Tests", (acceptanceTestIssues + integrationTestIssues))

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections                  : sections,
                additionalAcceptanceTests : numberAcceptanceTest - acceptanceTestIssues.count { !it.isMissing },
                additionalIntegrationTests: numberIntegrationTest - integrationTestIssues.count { !it.isMissing },
                testfiles                 : (acceptanceTestData + integrationTestData).testReportFiles.collect { file ->
                    [name: file.getName(), path: file.getPath()]
                },
                conclusion                : [
                    summary  : discrepancies.conclusion.summary,
                    statement: discrepancies.conclusion.statement
                ]
            ]
        ]

        if (!acceptanceTestIssues.isEmpty()) {
            data_.data.acceptanceTests = acceptanceTestIssues.collectEntries { testIssue ->
                [
                    testIssue.key,
                    [
                        key        : testIssue.key,
                        datetime   : testIssue.timestamp ? testIssue.timestamp.replaceAll("T", "</br>") : "N/A",
                        description: testIssue.description ?: "",
                        remarks    : testIssue.isMissing ? "not executed" : "",
                        risk_key   : testIssue.risks ? testIssue.risks.join(", ") : "N/A",
                        success    : testIssue.isSuccess ? "Y" : "N",
                        ur_key     : testIssue.requirements ? testIssue.requirements.join(", ") : "N/A"
                    ]
                ]
            }
        }

        if (!integrationTestIssues.isEmpty()) {
            data_.data.integrationTests = integrationTestIssues.collectEntries { testIssue ->
                [
                    testIssue.key,
                    [
                        key        : testIssue.key,
                        datetime   : testIssue.timestamp ? testIssue.timestamp.replaceAll("T", "</br>") : "N/A",
                        description: testIssue.description ?: "",
                        remarks    : testIssue.isMissing ? "not executed" : "",
                        risk_key   : testIssue.risks ? testIssue.risks.join(", ") : "N/A",
                        success    : testIssue.isSuccess ? "Y" : "N",
                        ur_key     : testIssue.requirements ? testIssue.requirements.join(", ") : "N/A"
                    ]
                ]
            }
        }

        def files = (acceptanceTestData + integrationTestData).testReportFiles.collectEntries { file ->
            ["raw/${file.getName()}", file.getBytes()]
        }

        def uri = this.createDocument(documentType, null, data_, files, null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createIVP(Map repo = null, Map data = null) {
        def documentType = DocumentType.IVP as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def installationTestIssues = this.project.getAutomatedTestsTypeInstallation()

        def testsGroupedByRepoType = groupTestsByRepoType(installationTestIssues)

        def testsOfRepoTypeOdsCode = []
        def testsOfRepoTypeOdsService = []
        testsGroupedByRepoType.each { repoTypes, tests ->
            if (repoTypes.contains(MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE)) {
                testsOfRepoTypeOdsCode.addAll(tests)
            }

            if (repoTypes.contains(MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_SERVICE)) {
                testsOfRepoTypeOdsService.addAll(tests)
            }
        }

        def data_ = [
            metadata: this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                repositories   : this.project.repositories.collect { [id: it.id, type: it.type, data: [git: [url: it.data.git == null ? null : it.data.git.url]]] },
                sections       : sections,
                tests          : SortUtil.sortIssuesByProperties(installationTestIssues.collect { testIssue ->
                    [
                        key     : testIssue.key,
                        summary : testIssue.name,
                        techSpec: testIssue.techSpecs.join(", ")
                    ]
                }, ["key"]),
                testsOdsService: testsOfRepoTypeOdsService,
                testsOdsCode   : testsOfRepoTypeOdsCode
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createTCR(Map repo = null, Map data = null) {
        String documentType = DocumentType.TCR as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def integrationTestData = data.tests.integration
        def acceptanceTestData = data.tests.acceptance
        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()
        def acceptanceTestIssues = this.project.getAutomatedTestsTypeAcceptance()

        def matchedHandler = { result ->
            result.each { testIssue, testCase ->
                testIssue.isSucess = !(testCase.error || testCase.failure || testCase.skipped)
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

        this.jiraUseCase.matchTestIssuesAgainstTestResults(integrationTestIssues, integrationTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)
        this.jiraUseCase.matchTestIssuesAgainstTestResults(acceptanceTestIssues, acceptanceTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def data_ = [
            metadata: this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections           : sections,
                integrationTests   : SortUtil.sortIssuesByProperties(integrationTestIssues.collect { testIssue ->
                    [
                        key         : testIssue.key,
                        description : testIssue.description,
                        requirements: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                        isSuccess   : testIssue.isSuccess,
                        bugs        : testIssue.bugs ? testIssue.bugs.join(", ") : "N/A",
                        steps       : testIssue.steps,
                        timestamp   : testIssue.timestamp ? testIssue.timestamp.replaceAll("T", " ") : "N/A"
                    ]
                }, ["key"]),
                acceptanceTests    : SortUtil.sortIssuesByProperties(acceptanceTestIssues.collect { testIssue ->
                    [
                        key         : testIssue.key,
                        description : testIssue.description,
                        requirements: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                        isSuccess   : testIssue.isSuccess,
                        bugs        : testIssue.bugs ? testIssue.bugs.join(", ") : "N/A",
                        steps       : testIssue.steps,
                        timestamp   : testIssue.timestamp ? testIssue.timestamp.replaceAll("T", " ") : "N/A"
                    ]
                }, ["key"])
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createTCP(Map repo = null, Map data = null) {
        String documentType = DocumentType.TCP as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def integrationTestIssues = this.project.getAutomatedTestsTypeIntegration()
        def acceptanceTestIssues = this.project.getAutomatedTestsTypeAcceptance()

        def data_ = [
            metadata: this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                sections        : sections,
                integrationTests: SortUtil.sortIssuesByProperties(integrationTestIssues.collect { testIssue ->
                    [
                        key         : testIssue.key,
                        description : testIssue.description,
                        requirements: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                        bugs        : testIssue.bugs ? testIssue.bugs.join(", ") : "N/A",
                        steps       : testIssue.steps
                    ]
                }, ["key"]),
                acceptanceTests : SortUtil.sortIssuesByProperties(acceptanceTestIssues.collect { testIssue ->
                    [
                        key         : testIssue.key,
                        description : testIssue.description,
                        requirements: testIssue.requirements ? testIssue.requirements.join(", ") : "N/A",
                        bugs        : testIssue.bugs ? testIssue.bugs.join(", ") : "N/A",
                        steps       : testIssue.steps
                    ]
                }, ["key"])
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, [:], null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }

    String createIVR(Map repo, Map data) {
        def documentType = DocumentType.IVR as String

        def watermarkText
        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        } else {
            watermarkText = this.getWatermarkText(documentType)
        }

        def installationTestData = data.tests.installation
        def installationTestIssues = this.project.getAutomatedTestsTypeInstallation()
        def numberOfInstallationTests = getNumberOfTest(installationTestData.testResults)

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

        this.jiraUseCase.matchTestIssuesAgainstTestResults(installationTestIssues, installationTestData?.testResults ?: [:], matchedHandler, unmatchedHandler)
        def discrepancies = this.computeTestDiscrepancies("Automated Installation Tests", installationTestIssues)
        def testsGroupedByRepoType = groupTestsByRepoType(installationTestIssues)

        def testsOfRepoTypeOdsCode = []
        def testsOfRepoTypeOdsService = []
        testsGroupedByRepoType.each { repoTypes, tests ->
            if (repoTypes.contains(MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE)) {
                testsOfRepoTypeOdsCode.addAll(tests)
            }

            if (repoTypes.contains(MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_SERVICE)) {
                testsOfRepoTypeOdsService.addAll(tests)
            }
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType]),
            data    : [
                repositories               : this.project.repositories.collect { [id: it.id, type: it.type, data: [git: [url: it.data.git == null ? null : it.data.git.url]]] },
                sections                   : sections,
                additionalInstallationTests: numberOfInstallationTests - installationTestIssues.count { !it.isMissing },
                tests                      : SortUtil.sortIssuesByProperties(installationTestIssues.collect { testIssue ->
                    [
                        key        : testIssue.key,
                        description: testIssue.description ?: "",
                        remarks    : testIssue.isMissing ? "not executed" : "",
                        success    : testIssue.isSuccess ? "Y" : "N",
                        summary    : testIssue.name,
                        techSpec   : testIssue.techSpecs.join(", ")
                    ]
                }, ["key"]),
                testfiles                  : installationTestData.testReportFiles.collect { file ->
                    [name: file.getName(), path: file.getPath()]
                },
                discrepancies              : discrepancies.discrepancies,
                conclusion                 : [
                    summary  : discrepancies.conclusion.summary,
                    statement: discrepancies.conclusion.statement
                ],
                testsOdsService            : testsOfRepoTypeOdsService,
                testsOdsCode               : testsOfRepoTypeOdsCode
            ]
        ]

        def files = data.tests.installation.testReportFiles.collectEntries { file ->
            ["raw/${file.getName()}", file.getBytes()]
        }

        def uri = this.createDocument(documentType, null, data_, files, null, null, watermarkText)
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
    }


    String createSSDS(Map repo = null, Map data = null) {
        def documentType = DocumentType.SSDS as String

        def sections = this.jiraUseCase.getDocumentChapterData(documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def componentsMetadata = SortUtil.sortIssuesByProperties(this.computeComponentMetadata(documentType).collect { it.value }, ["key"])
        def systemDesignSpecifications = this.project.getTechnicalSpecifications()
            .findAll { it.systemDesignSpec }
            .collect { techSpec ->
                [
                    key        : techSpec.key,
                    req_key    : techSpec.requirements.join(", "),
                    description: techSpec.systemDesignSpec
                ]
            }

        if (!sections."sec3s1") sections."sec3s1" = [:]
        sections."sec3s1".specifications = SortUtil.sortIssuesByProperties(systemDesignSpecifications, ["req_key", "key"])

        if (!sections."sec5s1") sections."sec5s1" = [:]
        sections."sec5s1".components = componentsMetadata.collect { c ->
            [key        : c.key, nameOfSoftware: c.nameOfSoftware, componentType: c.componentType,
             componentId: c.componentId, description: c.description, supplier: c.supplier,
             version    : c.version, references: c.references]
        }

        // Get the components that we consider modules in SSDS (the ones you have to code)
        def modules = componentsMetadata.findAll { it.odsRepoType.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE.toLowerCase() }.collect { component ->
            // We will set-up a double loop in the template. For moustache limitations we need to have lists
            component.requirements = component.requirements.collect { r ->
                [key: r.key, name: r.name, gampTopic: r.gampTopic]
            }.groupBy { it.gampTopic.toLowerCase() }.collect { k, v -> [gampTopic: k, requirementsofTopic: v] }
            component
        }
        if (!sections."sec10") sections."sec10" = [:]
        sections."sec10".modules = modules

        // Code review part
        // TODO append PDF files when issues with docx to PDF conversion is solved
        // since we can't convert the reports to PDF yet, plan B is to add them to the zip as docx
        def repos_ = (!repo) ? this.project.repositories : [repo]
        def files = obtainCodeReviewReport(repos_)
        def fileNames = files.collect { file -> [name: file.key] }
        if (!sections."sec14") sections."sec14" = [:]
        sections."sec14".referencedocs = fileNames

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            data    : [
                sections: sections
            ]
        ]

        def uri = this.createDocument(documentType, null, data_, files, null, null, this.getWatermarkText(documentType))
        this.notifyJiraTrackingIssue(documentType, "A new ${DOCUMENT_TYPE_NAMES[documentType]} has been generated and is available at: ${uri}.")
        return uri
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
            data    : [
                // TODO: change this.project.id to project_key in template
                project_key : this.project.key,
                // TODO: change data.project.repositories and data.repos to data.repositories in template
                repositories: this.project.repositories,
                sections    : sections
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
            metadata     : this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], repo),
            openShiftData: [
                ocpBuildId          : repo?.data.odsBuildArtifacts?."OCP Build Id" ?: "N/A",
                ocpDockerImage      : repo?.data.odsBuildArtifacts?."OCP Docker image" ?: "N/A",
                ocpDeploymentId     : repo?.data.odsBuildArtifacts?."OCP Deployment Id" ?: "N/A",
                podName             : pods?.items[0]?.metadata?.name ?: "N/A",
                podNamespace        : pods?.items[0]?.metadata?.namespace ?: "N/A",
                podCreationTimestamp: pods?.items[0]?.metadata?.creationTimestamp ?: "N/A",
                podEnvironment      : pods?.items[0]?.metadata?.labels?.env ?: "N/A",
                podNode             : pods?.items[0]?.spec?.nodeName ?: "N/A",
                podIp               : pods?.items[0]?.status?.podIP ?: "N/A",
                podStatus           : pods?.items[0]?.status?.phase ?: "N/A"
            ],
            data         : [
                repo    : repo,
                sections: sections
            ]
        ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, repo, data_, [:], modifier, null, watermarkText)
    }

    String createOverallDTR(Map repo = null, Map data = null) {
        def documentTypeName = DOCUMENT_TYPE_NAMES[DocumentType.OVERALL_DTR as String]
        def metadata = this.getDocumentMetadata(documentTypeName)

        def documentType = DocumentType.DTR as String

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

    private Map groupTestsByRepoType(List jiraTestIssues) {
        return jiraTestIssues.collect { test ->
            def components = test.getResolvedComponents()
            test.repoTypes = components.collect { component ->
                def normalizedComponentName = component.name.replaceAll("Technology-", "")
                def repository = project.repositories.find { repository ->
                    [repository.id, repository.name].contains(normalizedComponentName)
                }

                if (!repository) {
                    throw new IllegalArgumentException("Error: unable to create ${documentType}. Could not find a repository definition with id or name equal to '${normalizedComponentName}' for Jira component '${component.name}' in project '${this.project.id}'.")
                }

                return repository.type
            } as Set

            return test
        }.groupBy { it.repoTypes }
    }

    Map getDocumentMetadata(String documentTypeName, Map repo = null) {
        def name = this.project.name
        if (repo) {
            name += ": ${repo.id}"
        }

        def metadata = [
            id            : null, // unused
            name          : name,
            description   : this.project.description,
            type          : documentTypeName,
            version       : this.steps.env.RELEASE_PARAM_VERSION,
            date_created  : LocalDateTime.now().toString(),
            buildParameter: this.project.buildParams,
            git           : repo ? repo.data.git : this.project.gitData,
            jenkins       : [
                buildNumber: this.steps.env.BUILD_NUMBER,
                buildUrl   : this.steps.env.BUILD_URL,
                jobName    : this.steps.env.JOB_NAME
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
        if (environment.equals('D') && !LeVADocumentScheduler.ENVIRONMENT_TYPE['D'].containsKey(documentType)) {
            return this.DEVELOPER_PREVIEW_WATERMARK
        }

        return null
    }

    void notifyJiraTrackingIssue(String documentType, String message) {
        if (!this.jiraUseCase) return
        if (!this.jiraUseCase.jira) return

        def jiraDocumentLabel = this.getJiraTrackingIssueLabelForDocumentType(documentType)

        def jqlQuery = [jql: "project = ${project.key} AND issuetype = '${IssueTypes.LEVA_DOCUMENTATION}' AND labels = ${jiraDocumentLabel}"]

        // Search for the Jira issue associated with the document
        def jiraIssues = this.jiraUseCase.jira.getIssuesForJQLQuery(jqlQuery)
        if (jiraIssues.size() != 1) {
            throw new RuntimeException("Error: Jira query returned ${jiraIssues.size()} issues: '${jqlQuery}'.")
        }

        // Add a comment to the Jira issue with a link to the report
        this.jiraUseCase.jira.appendCommentToIssue(jiraIssues.first().key, message)
    }

    protected Integer getNumberOfTest(Map testResults) {
        def count = 0
        testResults.testsuites.each { testSuite ->
            count += testSuite.testcases.size()
        }

        return count
    }

}
