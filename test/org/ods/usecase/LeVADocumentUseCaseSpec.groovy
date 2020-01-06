package org.ods.usecase

import groovy.json.JsonOutput

import java.nio.file.Files

import org.ods.service.DocGenService
import org.ods.service.JenkinsService
import org.ods.service.LeVaDocumentChaptersFileService
import org.ods.service.NexusService
import org.ods.service.OpenShiftService
import org.ods.usecase.DocGenUseCase
import org.ods.usecase.JiraUseCase
import org.ods.usecase.SonarQubeUseCase
import org.ods.util.MROPipelineUtil
import org.ods.util.PDFUtil

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class LeVADocumentUseCaseSpec extends SpecHelper {

    def "compute test discrepancies"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def name = "myTests"

        when:
        def testIssues = createJiraTestIssues().each {
            it.isMissing = false
            it.isSuccess = true
        }

        def result = usecase.computeTestDiscrepancies(name, testIssues)

        then:
        result.discrepancies == "No discrepancies found."
        result.conclusion.summary == "Complete success, no discrepancies"
        result.conclusion.statement == "It is determined that all steps of the ${name} have been successfully executed and signature of this report verifies that the tests have been performed according to the plan. No discrepancies occurred."

        when:
        testIssues = createJiraTestIssues().each {
            it.isMissing = true
            it.isSuccess = false
        }

        result = usecase.computeTestDiscrepancies(name, testIssues)

        then:
        result.discrepancies == "The following minor discrepancies were found during testing: ${testIssues.collect { it.key }.join(", ")}."
        result.conclusion.summary == "Success - minor discrepancies found"
        result.conclusion.statement == "Some discrepancies were found as tests were not executed, this may be per design."

        when:
        testIssues = createJiraTestIssues().each {
            it.isMissing = false
            it.isSuccess = false
        }

        result = usecase.computeTestDiscrepancies(name, testIssues)

        then:
        result.discrepancies == "The following major discrepancies were found during testing: ${testIssues.collect { it.key }.join(", ")}."
        result.conclusion.summary == "No success - major discrepancies found"
        result.conclusion.statement == "Some discrepancies occured as tests did fail. It is not recommended to continue!"

        when:
        testIssues = createJiraTestIssues()
        testIssues[0..1].each {
            it.isMissing = true
            it.isSuccess = false
        }
        testIssues[2..4].each {
            it.isMissing = false
            it.isSuccess = false
        }

        result = usecase.computeTestDiscrepancies(name, testIssues)

        then:
        result.discrepancies == "The following major discrepancies were found during testing: ${testIssues.collect { it.key }.join(", ")}."
        result.conclusion.summary == "No success - major discrepancies found"
        result.conclusion.statement == "Some discrepancies occured as tests did fail. It is not recommended to continue!"
    }

    def "create CS"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.CS

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createCS(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getIssuesForProject(project.id, "${documentType}:Configurable Items", ["Configuration Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Interfaces",         ["Configuration Specification Task"], [], false, _) >> [:]
        0 * jira.getIssuesForProject(project.id, *_)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create DSD"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.DSD

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createDSD(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getIssuesForProject(project.id, null, ["System Design Specification Task"], [], false, _) >> [:]

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create DTP"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.DTP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]
        def testIssues = createJiraTestIssues()

        when:
        usecase.createDTP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getAutomatedTestIssues(project.id) >> testIssues
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create DTP without Jira"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        project.services.jira = null

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.DTP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]
        def testIssues = createJiraTestIssues()

        when:
        usecase.createDTP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType)
        1 * levaFiles.getDocumentChapterData(documentType) >> chapterData

        then:
        1 * jira.getAutomatedTestIssues(project.id) >> testIssues
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create DTR"() {
        given:
        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(steps, util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def xmlFile = Files.createTempFile("junit", ".xml").toFile()
        xmlFile << "<?xml version='1.0' ?>\n" + createJUnitXMLTestResults()

        def project = createProject()
        def repo = project.repositories.first()
        def testReportFiles = [xmlFile]
        def testResults = new JUnitTestReportsUseCase(steps).parseTestReportFiles(testReportFiles)
        def data = [
            testReportFiles: testReportFiles,
            testResults: testResults
        ]

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.DTR
        def files = [ "raw/${xmlFile.name}": xmlFile.bytes ]

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]
        def document = "myDocument".bytes
        def testIssues = createJiraTestIssues()

        when:
        usecase.createDTR(project, repo, data)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getAutomatedTestIssues(project.id, "Technology_${repo.id}") >> testIssues
        1 * jira.matchJiraTestIssuesAgainstTestResults(testIssues, testResults, _, _)
        //1 * usecase.computeTestDiscrepancies("Development Tests", testIssues)
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, files, _, null) >> document
        _ * util.getBuildParams() >> buildParams

        cleanup:
        xmlFile.delete()
    }

    def "create DTR without Jira"() {
        given:
        def steps = Spy(PipelineSteps)
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(steps, util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def xmlFile = Files.createTempFile("junit", ".xml").toFile()
        xmlFile << "<?xml version='1.0' ?>\n" + createJUnitXMLTestResults()

        def project = createProject()
        project.services.jira = null
        def repo = project.repositories.first()
        def testReportFiles = [xmlFile]
        def testResults = new JUnitTestReportsUseCase(steps).parseTestReportFiles(testReportFiles)
        def data = [
            testReportFiles: testReportFiles,
            testResults: testResults
        ]

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.DTR
        def files = [ "raw/${xmlFile.name}": xmlFile.bytes ]

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]
        def document = "myDocument".bytes
        def testIssues = createJiraTestIssues()

        when:
        usecase.createDTR(project, repo, data)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType)
        1 * levaFiles.getDocumentChapterData(documentType) >> chapterData

        then:
        1 * jira.getAutomatedTestIssues(project.id, "Technology_${repo.id}") >> testIssues
        1 * jira.matchJiraTestIssuesAgainstTestResults(testIssues, testResults, _, _)
        //1 * usecase.computeTestDiscrepancies("Development Tests", testIssues)
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, files, _, null) >> document
        _ * util.getBuildParams() >> buildParams
    }

    def "create FS"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.FS

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createFS(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getIssuesForProject(project.id, "${documentType}:Constraints",             ["Functional Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Data",                    ["Functional Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Function",                ["Functional Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Interfaces",              ["Functional Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Operational Environment", ["Functional Specification Task"], [], false, _) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Roles",                   ["Functional Specification Task"], [], false, _) >> [:]
        0 * jira.getIssuesForProject(project.id, *_)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create SCP"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.SCP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createSCP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create SCP without Jira"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        project.services.jira = null

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.SCP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createSCP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType)
        1 * levaFiles.getDocumentChapterData(documentType) >> chapterData

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create SCR"() {
        given:
        def buildParams = createBuildEnvironment(env)

        def steps = Spy(PipelineSteps)
        steps.env.BUILD_ID = "0815"

        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(steps, util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        def repo = project.repositories.first()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.SCR
        def sqReportsPath = "sonarqube/${repo.id}"
        def sqReportsStashName = "scrr-report-${repo.id}-${steps.env.BUILD_ID}"
        def files = [ "${usecase.getDocumentBasename("SCRR", buildParams.version, steps.env.BUILD_ID, project, repo)}.docx": getResource("Test.docx").bytes ]

        // Stubbed Method Responses
        def chapterData = ["sec1": "myContent"]
        def sqReportFiles = [ getResource("Test.docx") ]

        when:
        usecase.createSCR(project, repo)

        then:
        1 * jenkins.unstashFilesIntoPath(sqReportsStashName, "${steps.env.WORKSPACE}/${sqReportsPath}", "SonarQube Report") >> true
        1 * sq.loadReportsFromPath("${steps.env.WORKSPACE}/${sqReportsPath}") >> sqReportFiles

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, files, _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create SCR without Jira"() {
        given:
        def buildParams = createBuildEnvironment(env)

        def steps = Spy(PipelineSteps)
        steps.env.BUILD_ID = "0815"

        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(steps, util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        project.services.jira = null
        def repo = project.repositories.first()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.SCR
        def sqReportsPath = "sonarqube/${repo.id}"
        def sqReportsStashName = "scrr-report-${repo.id}-${steps.env.BUILD_ID}"
        def files = [ "${usecase.getDocumentBasename("SCRR", buildParams.version, steps.env.BUILD_ID, project, repo)}.docx": getResource("Test.docx").bytes ]

        // Stubbed Method Responses
        def chapterData = ["sec1": "myContent"]
        def sqReportFiles = [ getResource("Test.docx") ]

        when:
        usecase.createSCR(project, repo)

        then:
        1 * jenkins.unstashFilesIntoPath(sqReportsStashName, "${steps.env.WORKSPACE}/${sqReportsPath}", "SonarQube Report") >> true
        1 * sq.loadReportsFromPath("${steps.env.WORKSPACE}/${sqReportsPath}") >> sqReportFiles

        then:
        1 * jira.getDocumentChapterData(project.id, documentType)
        1 * levaFiles.getDocumentChapterData(documentType) >> chapterData

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, files, _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create SDS"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        def repo = project.repositories.first()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.SDS

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createSDS(project, repo)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create TIP"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.TIP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createTIP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create TIP without Jira"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        project.services.jira = null

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.TIP

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createTIP(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType)
        1 * levaFiles.getDocumentChapterData(documentType) >> chapterData

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create TIR"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        def repo = project.repositories.first()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.TIR

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createTIR(project, repo)

        then:
        1 * os.getPodDataForComponent(repo.id) >> createOpenShiftPodDataForComponent()

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create TIR without Jira"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()
        project.services.jira = null
        def repo = project.repositories.first()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.TIR

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createTIR(project, repo)

        then:
        1 * os.getPodDataForComponent(repo.id) >> createOpenShiftPodDataForComponent()

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project, repo)
        1 * usecase.createDocument(documentType, project, repo, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create URS"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.URS

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)
        def chapterData = ["sec1": "myContent"]

        when:
        usecase.createURS(project)

        then:
        1 * jira.getDocumentChapterData(project.id, documentType) >> chapterData
        0 * levaFiles.getDocumentChapterData(documentType)

        then:
        1 * jira.getIssuesForProject(project.id, "${documentType}:Availability",            ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Compatibility",           ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Interfaces",              ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Operational",             ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Operational Environment", ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Performance",             ["Epic"]) >> [:]
        1 * jira.getIssuesForProject(project.id, "${documentType}:Procedural Constraints",  ["Epic"]) >> [:]
        0 * jira.getIssuesForProject(project.id, *_)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createDocument(documentType, project, null, _, [:], _, null)
        _ * util.getBuildParams() >> buildParams
    }

    def "create overall DTR"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.OVERALL_DTR

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)

        when:
        usecase.createOverallDTR(project)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createOverallDocument("Overall-Cover", documentType, _, project)
        _ * util.getBuildParams() >> buildParams
    }

    def "create overall SCR"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.OVERALL_SCR

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)

        when:
        usecase.createOverallSCR(project)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createOverallDocument("Overall-Cover", documentType, _, project)
        _ * util.getBuildParams() >> buildParams
    }

    def "create overall SDS"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.OVERALL_SDS

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)

        when:
        usecase.createOverallSDS(project)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createOverallDocument("Overall-Cover", documentType, _, project)
        _ * util.getBuildParams() >> buildParams
    }

    def "create overall TIR"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        // Test Parameters
        def project = createProject()

        // Argument Constraints
        def documentType = LeVADocumentUseCase.DocumentTypes.OVERALL_TIR

        // Stubbed Method Responses
        def buildParams = createBuildEnvironment(env)

        when:
        usecase.createOverallTIR(project)

        then:
        1 * usecase.getDocumentMetadata(LeVADocumentUseCase.DOCUMENT_TYPE_NAMES[documentType], project)
        1 * usecase.createOverallDocument("Overall-TIR-Cover", documentType, _, project, _)
        _ * util.getBuildParams() >> buildParams
    }

    def "get supported documents"() {
        given:
        def util = Mock(MROPipelineUtil)
        def docGen = Mock(DocGenService)
        def jenkins = Mock(JenkinsService)
        def jira = Mock(JiraUseCase)
        def levaFiles = Mock(LeVaDocumentChaptersFileService)
        def nexus = Mock(NexusService)
        def os = Mock(OpenShiftService)
        def pdf = Mock(PDFUtil)
        def sq = Mock(SonarQubeUseCase)
        def usecase = Spy(new LeVADocumentUseCase(Spy(PipelineSteps), util, docGen, jenkins, jira, levaFiles, nexus, os, pdf, sq))

        when:
        def result = usecase.getSupportedDocuments()

        then:
        result.size() == 19

        then:
        result.containsKey("CS")
        result["CS"].toString().contains("LeVADocumentUseCase.createCS")

        then:
        result.containsKey("DSD")
        result["DSD"].toString().contains("LeVADocumentUseCase.createDSD")

        then:
        result.containsKey("DTP")
        result["DTP"].toString().contains("LeVADocumentUseCase.createDTP")

        then:
        result.containsKey("DTR")
        result["DTR"].toString().contains("LeVADocumentUseCase.createDTR")

        then:
        result.containsKey("FS")
        result["FS"].toString().contains("LeVADocumentUseCase.createFS")

        then:
        result.containsKey("FTP")
        result["FTP"].toString().contains("LeVADocumentUseCase.createFTP")

        then:
        result.containsKey("FTR")
        result["FTR"].toString().contains("LeVADocumentUseCase.createFTR")

        then:
        result.containsKey("IVP")
        result["IVP"].toString().contains("LeVADocumentUseCase.createIVP")

        then:
        result.containsKey("IVR")
        result["IVR"].toString().contains("LeVADocumentUseCase.createIVR")

        then:
        result.containsKey("SCP")
        result["SCP"].toString().contains("LeVADocumentUseCase.createSCP")

        then:
        result.containsKey("SCR")
        result["SCR"].toString().contains("LeVADocumentUseCase.createSCR")

        then:
        result.containsKey("SDS")
        result["SDS"].toString().contains("LeVADocumentUseCase.createSDS")

        then:
        result.containsKey("TIP")
        result["TIP"].toString().contains("LeVADocumentUseCase.createTIP")

        then:
        result.containsKey("TIR")
        result["TIR"].toString().contains("LeVADocumentUseCase.createTIR")

        then:
        result.containsKey("URS")
        result["URS"].toString().contains("LeVADocumentUseCase.createURS")

        then:
        result.containsKey("OVERALL_DTR")
        result["OVERALL_DTR"].toString().contains("LeVADocumentUseCase.createOverallDTR")

        then:
        result.containsKey("OVERALL_SCR")
        result["OVERALL_SCR"].toString().contains("LeVADocumentUseCase.createOverallSCR")

        then:
        result.containsKey("OVERALL_SDS")
        result["OVERALL_SDS"].toString().contains("LeVADocumentUseCase.createOverallSDS")

        then:
        result.containsKey("OVERALL_TIR")
        result["OVERALL_TIR"].toString().contains("LeVADocumentUseCase.createOverallTIR")
    }
}
