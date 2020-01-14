package org.ods.usecase

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

import groovy.json.JsonOutput

class LeVADocumentUseCase extends DocGenUseCase {

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
        (DocumentType.OVERALL_SCR as String): "Overall Software Development (Coding and Code Review) Report",
        (DocumentType.OVERALL_SDS as String): "Overall Software Design Specification",
        (DocumentType.OVERALL_TIR as String): "Overall Technical Installation Report"
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
        def documentType = DocumentType.CS as String

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
                // Remove the Technology- prefix for ODS components
                def matcher = name =~ /^Technology-/
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
        def documentType = DocumentType.DSD as String

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

        // A mapping of component names starting with Technology- to issues
        def specificationsForTechnologyComponents = specifications.findAll { it.key.startsWith("Technology-") }

        // A mapping of component names to corresponding repository metadata
        def componentsMetadata = specificationsForTechnologyComponents.collectEntries { componentName, issues ->
            def normalizedComponentName = componentName.replaceAll("Technology-", "")

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
            // Create a collection of disjoint issues across all components starting with Technology-
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
        def documentType = DocumentType.DTP as String

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        // TODO: get automated test issues of type InstallationTest
        def data = [
            metadata: this.getDocumentMetadata(this.DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                project: project,
                sections: sections,
                tests: this.jira.getAutomatedTestIssues(project.id, null, ["UnitTest"]).collectEntries { issue ->
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
        def documentType = DocumentType.DTR as String

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def jiraTestIssues = this.jira.getAutomatedTestIssues(project.id, "Technology-${repo.id}", ["UnitTest"])

        def matchedHandler = { result ->
            result.each { issue, testcase ->
                issue.test.isSuccess = !(testcase.error || testcase.failure || testcase.skipped)
                issue.test.isMissing = false
            }
        }

        def unmatchedHandler = { result ->
            result.each { issue ->
                issue.test.isSuccess = false
                issue.test.isMissing = true
            }
        }

        this.jira.matchJiraTestIssuesAgainstTestResults(jiraTestIssues, data?.testResults ?: [:], matchedHandler, unmatchedHandler)

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
                            description: issue.test.description ?: "",
                            isRelatedTo: issue.issuelinks ? issue.issuelinks.first().issue.key : "N/A",
                            success: issue.test.isSuccess ? "Y" : "N",
                            remarks: issue.test.isMissing ? "not executed" : ""
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
        def documentType = DocumentType.FS as String

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
        return "http://nexus"
    }

    String createFTR(Map project, Map repo, Map data) {
        // TODO: not yet implemented
        return "http://nexus"
    }

    String createIVP(Map project) {
        def documentType = DocumentType.IVP as String

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            throw new RuntimeException("Error: unable to create ${documentType}. Could not obtain document chapter data from Jira.")
        }

        def data = [
            metadata: this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project),
            data: [
                project: project,
                sections: sections,
                tests: this.jira.getAutomatedTestIssues(project.id, null, ["InstallationTest"]).collectEntries { issue ->
                    [
                        issue.key,
                        [
                            key: issue.key,
                            summary: issue.summary,
                            test: issue.test
                        ]
                    ]
                }
            ]
        ]

        return this.createDocument(documentType, project, null, data, [:], null, null)
    }

    String createIVR(Map project, Map repo, Map data) {
        def documentType = DocumentType.IVR as String

        def sections = this.jira.getDocumentChapterData(project.id, documentType)
        if (!sections) {
            sections = this.levaFiles.getDocumentChapterData(documentType)
        }

        def jiraTestIssues = this.jira.getAutomatedTestIssues(project.id, "Technology-${repo.id}", ["InstallationTest"])

        def matchedHandler = { result ->
            result.each { issue, testcase ->
                issue.test.isSuccess = !(testcase.error || testcase.failure || testcase.skipped)
                issue.test.isMissing = false
            }
        }

        def unmatchedHandler = { result ->
            result.each { issue ->
                issue.test.isSuccess = false
                issue.test.isMissing = true
            }
        }

        this.jira.matchJiraTestIssuesAgainstTestResults(jiraTestIssues, data?.testResults ?: [:], matchedHandler, unmatchedHandler)

        def discrepancies = this.computeTestDiscrepancies("Installation and Configuration Tests", jiraTestIssues)

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
                            description: issue.test.description ?: "",
                            isRelatedTo: issue.issuelinks ? issue.issuelinks.first().issue.key : "N/A",
                            summary: issue.summary,
                            success: issue.test.isSuccess ? "Y" : "N",
                            remarks: issue.test.isMissing ? "not executed" : ""
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

    String createSCP(Map project) {
        def documentType = DocumentType.SCP as String

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
        def documentType = DocumentType.SCR as String

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
        def documentType = DocumentType.SDS as String

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
        def documentType = DocumentType.TIP as String

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
        def documentType = DocumentType.TIR as String

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
        def documentType = DocumentType.URS as String

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
                // Remove the Technology- prefix for ODS components
                def matcher = name =~ /^Technology-/
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
        def documentType = DocumentType.OVERALL_DTR as String
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallSCR(Map project) {
        def documentType = DocumentType.OVERALL_SCR as String
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallSDS(Map project) {
        def documentType = DocumentType.OVERALL_SDS as String
        def metadata = this.getDocumentMetadata(DOCUMENT_TYPE_NAMES[documentType], project)
        return this.createOverallDocument("Overall-Cover", documentType, metadata, project)
    }

    String createOverallTIR(Map project) {
        def documentType = DocumentType.OVERALL_TIR as String
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
            version: this.steps.env.RELEASE_PARAM_VERSION,
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

    List<String> getSupportedDocuments() {
        return DocumentType.values().collect { it as String }
    }
}
