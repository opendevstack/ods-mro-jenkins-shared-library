package org.ods.usecase

import com.cloudbees.groovy.cps.NonCPS

import java.time.LocalDateTime

import org.apache.commons.io.FilenameUtils
import org.ods.service.DocGenService
import org.ods.service.JenkinsService
import org.ods.service.LeVADocumentChaptersFileService
import org.ods.service.NexusService
import org.ods.service.OpenShiftService
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil
import org.ods.util.PDFUtil
import org.ods.util.SortUtil

class LeVADocumentUseCase extends DocGenUseCase {

    class DocumentTypes {
        static final String CS = "CS"
        static final String DSD = "DSD"
        static final String DTP = "DTP"
        static final String DTR = "DTR"
        static final String FS = "FS"
        static final String FTP = "FTP"
        static final String FTR = "FTR"
        static final String IVP = "IVP"
        static final String IVR = "IVR"
        static final String SCP = "SCP"
        static final String SCR = "SCR"
        static final String SDS = "SDS"
        static final String TIP = "TIP"
        static final String TIR = "TIR"
        static final String URS = "URS"

        static final String OVERALL_DTR = "OVERALL_DTR"
        static final String OVERALL_SCR = "OVERALL_SCR"
        static final String OVERALL_SDS = "OVERALL_SDS"
        static final String OVERALL_TIR = "OVERALL_TIR"
    }

    private static Map DOCUMENT_TYPE_NAMES = [
        (DocumentTypes.CS): "Configuration Specification",
        (DocumentTypes.DSD): "System Design Specification",
        (DocumentTypes.DTP): "Software Development Testing Plan",
        (DocumentTypes.DTR): "Software Development Testing Report",
        (DocumentTypes.FS): "Functional Specification",
        (DocumentTypes.FTP): "Functional and Requirements Testing Plan",
        (DocumentTypes.FTR): "Functional and Requirements Testing Report",
        (DocumentTypes.IVP): "Configuration and Installation Testing Plan",
        (DocumentTypes.IVR): "Configuration and Installation Testing Report",
        (DocumentTypes.SCP): "Software Development (Coding and Code Review) Plan",
        (DocumentTypes.SCR): "Software Development (Coding and Code Review) Report",
        (DocumentTypes.SDS): "Software Design Specification",
        (DocumentTypes.TIP): "Technical Installation Plan",
        (DocumentTypes.TIR): "Technical Installation Report",
        (DocumentTypes.URS): "User Requirements Specification",
        (DocumentTypes.OVERALL_DTR): "Overall Software Development Testing Report",
        (DocumentTypes.OVERALL_SCR): "Overall Software Development (Coding and Code Review) Report",
        (DocumentTypes.OVERALL_SDS): "Overall Software Design Specification",
        (DocumentTypes.OVERALL_TIR): "Overall Technical Installation Report"
    ]

    private JenkinsService jenkins
    private JiraUseCase jira
    private LeVADocumentChaptersFileService levaFiles
    private OpenShiftService os
    private SonarQubeUseCase sq

    LeVADocumentUseCase(IPipelineSteps steps, MROPipelineUtil util, DocGenService docGen, JenkinsService jenkins, JiraUseCase jira, LeVADocumentChaptersFileService levaFiles, NexusService nexus, OpenShiftService os, PDFUtil pdf, SonarQubeUseCase sq) {
        super(steps, util, docGen, nexus, pdf)
        this.jenkins = jenkins
        this.jira = jira
        this.levaFiles = levaFiles
        this.os = os
        this.sq = sq
    }

    private Map computeTestDiscrepancies(String name, List jiraTestIssues) {
        def result = [
            discrepancies: "No discrepancies found.",
            conclusion: [
                summary: "Complete success, no discrepancies",
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

    String createCS(Map project) {
        def documentType = DocumentTypes.CS

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // Configurable Items
        def configurableItems = this.jira.getIssuesForProject(project.id, "${documentType}:Configurable Items", ["Configuration Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }.findAll { it.key != "${documentType}:Configurable Items" }

        if (!sections."sec3") {
            sections."sec3" = [:]
        }

        if (!configurableItems.isEmpty()) {
            def configurableItemsIssuesList = configurableItems.collect { name, issues ->
                // Remove the Technology_ prefix for ODS components
                def matcher = name =~ /^Technology_/
                if (matcher.find()) {
                    name = matcher.replaceAll("")
                }

                // Reduce the issues to the data points required by the document
                def items = issues.collect { issue ->
                    return issue.subMap(["key", "description"]) << [
                        // Map the key of a linked user requirement
                        ur_key: issue.issuelinks.first().issue.key
                    ]
                }

                return [
                    name: name,
                    items: SortUtil.sortIssuesByProperties(items, ["ur_key", "key"])
                ]
            }

            sections."sec3".components = SortUtil.sortIssuesByProperties(configurableItemsIssuesList, ["name"])
        }

        // Interfaces
        def interfaces = this.jira.getIssuesForProject(project.id, "${documentType}:Interfaces", ["Configuration Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        if (!sections."sec4") {
            sections."sec4" = [:]
        }

        if (!interfaces.isEmpty()) {
            def interfacesIssuesList = interfaces["${documentType}:Interfaces"].collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec4".items = SortUtil.sortIssuesByProperties(interfacesIssuesList, ["ur_key", "key"])
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createDSD(Map project) {
        def documentType = DocumentTypes.DSD

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // A mapping of component names to issues
        def specifications = this.jira.getIssuesForProject(project.id, null, ["System Design Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        // System Design Specifications
        if (!sections."sec3") {
            sections."sec3" = [:]
        }

        if (!specifications.isEmpty()) {
            // Create a collection of disjoint issues across all components
            def specificationsIssuesList = specifications.values().flatten().toSet()
            
            specificationsIssuesList = specificationsIssuesList.collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec3".specifications = SortUtil.sortIssuesByProperties(specificationsIssuesList, ["ur_key", "key"])
        }

        // A mapping of component names starting with Technology_ to issues
        def specificationsForTechnologyComponents = specifications.findAll { it.key.startsWith("Technology_") }

        // A mapping of component names to corresponding repository metadata
        def componentsMetadata = specificationsForTechnologyComponents.collectEntries { componentName, issues ->
            def normalizedComponentName = componentName.replaceAll("Technology_", "")

            def repo = project.repositories.find { [it.id, it.name].contains(normalizedComponentName) }
            if (!repo) {
                throw new RuntimeException("Error: unable to create ${documentType}. Could not find a repository definition with id or name equal to '${normalizedComponentName}' for Jira component '${componentName}' in project '${project.id}'.")
            }

            def metadata = repo.pipelineConfig.metadata

            return [
                componentName,
                [
                    componentId: metadata.id ?: "N/A - part of this application",
                    componentType: (repo.type?.toLowerCase() == MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE) ? "ODS Component" : "Software",
                    description: metadata.description,
                    nameOfSoftware: metadata.name,
                    references: metadata.references ?: "N/A",
                    supplier: metadata.supplier,
                    version: metadata.version
                ]
            ]
        }

        // System Components List
        if (!sections."sec5s1") {
            sections."sec5s1" = [:]
        }

        if (!specificationsForTechnologyComponents.isEmpty()) {
            // Create a collection of disjoint issues across all components starting with Technology_
            def specificationsForTechnologyComponentsIssuesList = specificationsForTechnologyComponents.values().flatten().toSet()

            specificationsForTechnologyComponentsIssuesList = specificationsForTechnologyComponentsIssuesList.collect { issue ->
                // Reduce the issues to the data points required by the document
                def result = issue.subMap(["key"])

                // Mix-in compnoent metadata
                def componentName = issue.components.first()
                result << componentsMetadata[componentName]

                return result
            }

            sections."sec5s1".specifications = SortUtil.sortIssuesByProperties(specificationsForTechnologyComponentsIssuesList, ["key"])
        }

        // System Components Specification (fully contained in data for System Components List)

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createDTP(Map project) {
        def documentType = DocumentTypes.DTP

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                project: project,
                sections: sections,
                tests: this.jira.getAutomatedTestIssues(project.id).collectEntries { issue ->
                    [
                        issue.key,
                        [
                            key: issue.key,
                            description: issue.description ?: "",
                            isRelatedTo: issue.issuelinks ? issue.issuelinks.first().issue.key : "N/A"
                        ]
                    ]
                }
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createDTR(Map project, Map repo, Map data) {
        def documentType = DocumentTypes.DTR

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def jiraTestIssues = this.jira.getAutomatedTestIssues(project.id, "Technology_${repo.id}")

        def matchedHandler = { result ->
            result.each { issue, testcase ->
                issue.isSuccess = !(testcase.error || testcase.failure || testcase.skipped)
                issue.isMissing = false
            }
        }

        def unmatchedHandler = { result ->
            result.each { issue ->
                issue.isSuccess = false
                issue.isMissing = true
            }
        }

        this.jira.matchJiraTestIssuesAgainstTestResults(jiraTestIssues, data.testResults, matchedHandler, unmatchedHandler)

        def discrepancies = this.computeTestDiscrepancies("Development Tests", jiraTestIssues)

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project, repo),
            data: [
                repo: repo,
                sections: sections,
                tests: jiraTestIssues.collectEntries { issue ->
                    [
                        issue.key,
                        [
                            key: issue.key,
                            description: issue.description ?: "",
                            isRelatedTo: issue.issuelinks ? issue.issuelinks.first().issue.key : "N/A",
                            success: issue.isSuccess ? "Y" : "N",
                            remarks: issue.isMissing ? "not executed" : ""
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

        return this.createDocument(documentType, project, repo, data_, files, modifier, null)
    }

    String createFS(Map project) {
        def documentType = DocumentTypes.FS

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // Constraints
        def constraints = this.jira.getIssuesForProject(project.id, "${documentType}:Constraints", ["Functional Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        if (!sections."sec8") {
            sections."sec8" = [:]
        }

        if (!constraints.isEmpty()) {
            def constraintsIssuesList = constraints["${documentType}:Constraints"].collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec8".items = SortUtil.sortIssuesByProperties(constraintsIssuesList, ["ur_key", "key"])
        }

        // Data
        def data = this.jira.getIssuesForProject(project.id, "${documentType}:Data", ["Functional Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        if (!sections."sec5") {
            sections."sec5" = [:]
        }

        if (!data.isEmpty()) {
            def dataIssuesList = data["${documentType}:Data"].collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec5".items = SortUtil.sortIssuesByProperties(dataIssuesList, ["ur_key", "key"])
        }

        // Function
        def functions = this.jira.getIssuesForProject(project.id, "${documentType}:Function", ["Functional Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }.findAll { it.key != "${documentType}:Function" }

        if (!sections."sec3") {
            sections."sec3" = [:]
        }

        if (!functions.isEmpty()) {
            def functionsIssuesList = functions.collect { name, issues ->
                // Reduce the issues to the data points required by the document
                def items = issues.collect { issue ->
                    return issue.subMap(["key", "description"]) << [
                        // Map the key of a linked user requirement
                        ur_key: issue.issuelinks.first().issue.key
                    ]
                }

                return [
                    name: name,
                    items: SortUtil.sortIssuesByProperties(items, ["ur_key", "key"])
                ]
            }

            sections."sec3".components = SortUtil.sortIssuesByProperties(functionsIssuesList, ["name"])
        }

        // Interfaces
        def interfaces = this.jira.getIssuesForProject(project.id, "${documentType}:Interfaces", ["Functional Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        if (!sections."sec6") {
            sections."sec6" = [:]
        }

        if (!interfaces.isEmpty()) {
            def interfacesIssuesList = interfaces["${documentType}:Interfaces"].collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec6".items = SortUtil.sortIssuesByProperties(interfacesIssuesList, ["ur_key", "key"])
        }

        // Operational Environment
        def environment = this.jira.getIssuesForProject(project.id, "${documentType}:Operational Environment", ["Functional Specification Task"], [], false) { issuelink ->
            return issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }

        if (!sections."sec7") {
            sections."sec7" = [:]
        }

        if (!environment.isEmpty()) {
            def environmentIssuesList = environment["${documentType}:Operational Environment"].collect { issue ->
                // Reduce the issues to the data points required by the document
                return issue.subMap(["key", "description"]) << [
                    // Map the key of a linked user requirement
                    ur_key: issue.issuelinks.first().issue.key
                ]
            }

            sections."sec7".items = SortUtil.sortIssuesByProperties(environmentIssuesList, ["ur_key", "key"])
        }

        // Roles
        def roles = this.jira.getIssuesForProject(project.id, "${documentType}:Roles", ["Functional Specification Task"], [], false) { issuelink ->
            return (issuelink.type.relation == "specifies" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")) || (issuelink.type.relation == "is detailed by" && (issuelink.issue.issuetype.name == "Functional Specification Task"))
        }

        if (!sections."sec4") {
            sections."sec4" = [:]
        }

        if (!roles.isEmpty()) {
            def rolesIssuesList = roles["${documentType}:Roles"].collect { issue ->
                // Find the user requirement this functional specification task specifies
                def epic = issue.issuelinks.find { it.type.relation == "specifies" }.issue

                // Find the functional specification tasks that provide further detail to this task
                def items = issue.issuelinks.findAll { it.type.relation == "is detailed by" }.collect { issuelink ->
                    // Reduce the issues to the data points required by the document
                    return issuelink.issue.subMap(["key", "description"]) << [
                        // Map the key of the linked user requirement
                        ur_key: epic.key
                    ]
                }

                // Reduce the issues to the data points required by the document
                return issue.subMap(["key"]) << [
                    name: issue.summary,
                    items: SortUtil.sortIssuesByProperties(items, ["ur_key", "key"])
                ]
            }

            def index = 0
            sections."sec4".roles = SortUtil.sortIssuesByProperties(rolesIssuesList, ["name"]).collect { issue ->
                // Add a custom heading number
                issue << [ number: ++index ]
            }
        }

        def data_ = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data_, [:], null, null)
    }

    String createFTP(Map project) {
        // TODO: not yet implemented
    }

    String createFTR(Map project, Map repo, Map data) {
        // TODO: not yet implemented
    }

    String createIVP(Map project) {
        // TODO: not yet implemented
    }

    String createIVR(Map project, Map repo, Map data) {
        // TODO: not yet implemented
    }

    String createSCP(Map project) {
        def documentType = DocumentTypes.SCP

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                project: project,
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createSCR(Map project, Map repo) {
        def documentType = DocumentTypes.SCR

        def sqReportsPath = "sonarqube/${repo.id}"
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

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project, repo),
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
        def name = this.getDocumentBasename("SCRR", this.util.getBuildParams().version, this.steps.env.BUILD_ID, project, repo)
        def sqReportFile = sqReportFiles.first()
        files << [ "${name}.${FilenameUtils.getExtension(sqReportFile.getName())}": sqReportFile.getBytes() ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, project, repo, data, files, modifier, null)
    }

    String createSDS(Map project, Map repo) {
        def documentType = DocumentTypes.SDS

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project, repo),
            data: [
                repo: repo,
                sections: sections
            ]
        ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, project, repo, data, [:], modifier, null)
    }

    String createTIP(Map project) {
        def documentType = DocumentTypes.TIP

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                project: project,
                repos: project.repositories,
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createTIR(Map project, Map repo) {
        def documentType = DocumentTypes.TIR

        def pods = this.os.getPodDataForComponent(repo.id)

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project, repo),
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
                project: project,
                repo: repo,
                sections: sections
            ]
        ]

        def modifier = { document ->
            repo.data.documents[documentType] = document
            return document
        }

        return this.createDocument(documentType, project, repo, data, [:], modifier, null)
    }

    String createURS(Map project) {
        def documentType = DocumentTypes.URS

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        // Availability
        def availability = this.jira.getIssuesForProject(project.id, "${documentType}:Availability", ["Epic"])

        if (!sections."sec3s3s2") {
            sections."sec3s3s2" = [:]
        }

        if (!availability.isEmpty()) {
            def availabilityIssuesList = availability["${documentType}:Availability"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec3s3s2".requirements = SortUtil.sortIssuesByProperties(availabilityIssuesList, ["key"])
        }

        // Compatibility
        def compatibility = this.jira.getIssuesForProject(project.id, "${documentType}:Compatibility", ["Epic"])

        if (!sections."sec4s1") {
            sections."sec4s1" = [:]
        }

        if (!compatibility.isEmpty()) {
            def compatibilityIssuesList = compatibility["${documentType}:Compatibility"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec4s1".requirements = SortUtil.sortIssuesByProperties(compatibilityIssuesList, ["key"])
        }

        // Interfaces
        def interfaces = this.jira.getIssuesForProject(project.id, "${documentType}:Interfaces", ["Epic"])

        if (!sections."sec3s4") {
            sections."sec3s4" = [:]
        }

        if (!interfaces.isEmpty()) {
            def interfacesIssuesList = interfaces["${documentType}:Interfaces"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec3s4".requirements = SortUtil.sortIssuesByProperties(interfacesIssuesList, ["key"])
        }

        // Operational
        def operational = this.jira.getIssuesForProject(project.id, "${documentType}:Operational", ["Epic"])
            .findAll { it.key != "${documentType}:Operational" }

        if (!sections."sec3s2") {
            sections."sec3s2" = [:]
        }

        if (!operational.isEmpty()) {
            def operationalIssuesList = operational.collect { name, epics ->
                // Remove the Technology_ prefix for ODS components
                def matcher = name =~ /^Technology_/
                if (matcher.find()) {
                    name = matcher.replaceAll("")
                }

                // Reduce the issues to the data points required by the document
                def requirements = epics.collect { epic ->
                    def issues = epic.issues.collect { story ->
                        return story.subMap(["key", "description"])
                    }

                    return epic.subMap(["key", "description"]) << [
                        issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                    ]
                }

                return [
                    name: name,
                    requirements: SortUtil.sortIssuesByProperties(requirements, ["key"])
                ]
            }

            sections."sec3s2".components = SortUtil.sortIssuesByProperties(operationalIssuesList, ["name"])
        }

        // Operational Environment
        def environment = this.jira.getIssuesForProject(project.id, "${documentType}:Operational Environment", ["Epic"])

        if (!sections."sec3s5") {
            sections."sec3s5" = [:]
        }

        if (!environment.isEmpty()) {
            def environmentIssuesList = environment["${documentType}:Operational Environment"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec3s5".requirements = SortUtil.sortIssuesByProperties(environmentIssuesList, ["key"])
        }

        // Performance
        def performance = this.jira.getIssuesForProject(project.id, "${documentType}:Performance", ["Epic"])

        if (!sections."sec3s3s1") {
            sections."sec3s3s1" = [:]
        }

        if (!performance.isEmpty()) {
            def performanceIssuesList = performance["${documentType}:Performance"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec3s3s1".requirements = SortUtil.sortIssuesByProperties(performanceIssuesList, ["key"])
        }

        // Procedural Constraints
        def procedural = this.jira.getIssuesForProject(project.id, "${documentType}:Procedural Constraints", ["Epic"])

        if (!sections."sec4s2") {
            sections."sec4s2" = [:]
        }

        if (!procedural.isEmpty()) {
            def proceduralIssuesList = procedural["${documentType}:Procedural Constraints"].collect { epic ->
                // Reduce the issues to the data points required by the document
                def issues = epic.issues.collect { story ->
                    return story.subMap(["key", "description"])
                }

                return epic.subMap(["key", "description"]) << [
                    issues: SortUtil.sortIssuesByProperties(issues, ["key"])
                ]
            }

            sections."sec4s2".requirements = SortUtil.sortIssuesByProperties(proceduralIssuesList, ["key"])
        }

        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                sections: sections
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createOverallDTR(Map project) {
        def documentType = DocumentTypes.OVERALL_DTR
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallSCR(Map project) {
        def documentType = DocumentTypes.OVERALL_SCR
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallSDS(Map project) {
        def documentType = DocumentTypes.OVERALL_SDS
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallTIR(Map project) {
        def documentType = DocumentTypes.OVERALL_TIR
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-TIR-Cover", documentType, metadata, project) { data ->
            // Append another section for the Jenkins build log
            data.sections << [
                heading: "Jenkins Build Log"
            ]

            // Add Jenkins build log data
            data.jenkinsData = [
                log: this.jenkins.getCurrentBuildLogAsText()
            ]
        }
    }

    Map getDocumentMetadata(String documentTypeName, Map project, Map repo = null) {
        def name = project.name
        if (repo) {
            name += ": ${repo.id}"
        }

        return [
            id: null, // unused
            name: name,
            description: project.description,
            type: documentTypeName,
            version: null, // unused
            date_created: LocalDateTime.now().toString(),
            buildParameter: this.util.getBuildParams(),
            git: repo ? repo.data.git : project.data.git,
            jenkins: [
                buildNumber: this.steps.env.BUILD_NUMBER,
                buildUrl: this.steps.env.BUILD_URL,
                jobName: this.steps.env.JOB_NAME
            ]
        ]
    }

    Map<String, MetaMethod> getSupportedDocuments() {
        return this.metaClass.methods
            .findAll { method ->
                return method.isPublic() && (method.getName() ==~ /^create[A-Z]{2,}/ || method.getName() ==~ /^createOverall[A-Z]{2,}/)
            }
            .collectEntries { method ->
                def name = method.getName()
                    .replaceAll("create", "")
                    .replaceAll("Overall", "Overall_")
                    .toUpperCase()

                return [ name, method ]
            }
    }
}
