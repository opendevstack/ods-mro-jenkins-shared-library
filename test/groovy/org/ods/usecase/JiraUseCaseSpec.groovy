package org.ods.usecase


import org.ods.service.JiraService
import org.ods.util.GitUtil
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil
import org.ods.util.Project
import spock.lang.Ignore
import util.FakeProject
import util.SpecHelper

import static util.FixtureHelper.*

class JiraUseCaseSpec extends SpecHelper {

    GitUtil git
    JiraService jira
    IPipelineSteps steps
    MROPipelineUtil util
    JiraUseCase usecase
    Project project

    def setup() {
        steps = Spy(util.PipelineSteps)
        git = Mock(GitUtil)
        util = Mock(MROPipelineUtil)
        jira = Mock(JiraService)
    }

    def "apply test results as test issue labels"() {
        given:
        def testIssues = createJiraTestIssues()
        def testResults = createTestResults()

        when:
        usecase.applyXunitTestResultsAsTestIssueLabels(testIssues, testResults)

        then:
        1 * jira.removeLabelsFromIssue("JIRA-1", { return it == JiraUseCase.TestIssueLabels.values()*.toString() })
        1 * jira.addLabelsToIssue("JIRA-1", ["Succeeded"])
        0 * jira.addLabelsToIssue("JIRA-1", _)

        then:
        1 * jira.removeLabelsFromIssue("JIRA-2", { it == JiraUseCase.TestIssueLabels.values()*.toString() })
        1 * jira.addLabelsToIssue("JIRA-2", ["Error"])
        0 * jira.addLabelsToIssue("JIRA-2", _)

        then:
        1 * jira.removeLabelsFromIssue("JIRA-3", { it == JiraUseCase.TestIssueLabels.values()*.toString() })
        1 * jira.addLabelsToIssue("JIRA-3", ["Failed"])
        0 * jira.addLabelsToIssue("JIRA-3", _)

        then:
        1 * jira.removeLabelsFromIssue("JIRA-4", { it == JiraUseCase.TestIssueLabels.values()*.toString() })
        1 * jira.addLabelsToIssue("JIRA-4", ["Skipped"])
        0 * jira.addLabelsToIssue("JIRA-4", _)

        then:
        1 * jira.removeLabelsFromIssue("JIRA-5", { it == JiraUseCase.TestIssueLabels.values()*.toString() })
        1 * jira.addLabelsToIssue("JIRA-5", ["Missing"])
        0 * jira.addLabelsToIssue("JIRA-5", _)
    }

    def "check Jira issue matches test case"() {
        when:
        def issue = [key: "JIRA-123"]
        def testcase = [name: "JIRA123 test"]

        then:
        usecase.checkTestsIssueMatchesTestCase(issue, testcase)

        when:
        issue = [key: "JIRA-123"]
        testcase.ame = "JIRA123-test"

        then:
        usecase.checkTestsIssueMatchesTestCase(issue, testcase)

        when:
        issue = [key: "JIRA-123"]
        testcase.ame = "JIRA123_test"

        then:
        usecase.checkTestsIssueMatchesTestCase(issue, testcase)

        when:
        issue = [key: "JIRA-123"]
        testcase.name = "JIRA123test"

        then:
        !usecase.checkTestsIssueMatchesTestCase(issue, testcase)

        when:
        issue = [key: "JIRA-123"]
        testcase.name = "JIRA-123_test"

        then:
        !usecase.checkTestsIssueMatchesTestCase(issue, testcase)
    }

    def "create bugs and block impacted test cases"() {
        given:
        // Test Parameters
        def testIssues = createJiraTestIssues()
        def failures = createTestResultFailures()
        def comment = "myComment"

        // Stubbed Method Responses
        def bug = [key: "JIRA-BUG"]

        when:
        usecase.createBugsForFailedTestIssues(testIssues, failures, comment)

        then:
        1 * jira.createIssueTypeBug(project.key, failures.first().type, failures.first().text) >> bug

        then:
        1 * jira.createIssueLinkTypeBlocks(bug, {
            // the Jira issue that shall be linked to the bug
            it.key == "JIRA-3"
        })

        then:
        1 * jira.appendCommentToIssue(bug.key, comment)
    }

    def "get document chapter data"() {
        given:
        // Test Parameters
        def documentType = "myDocumentType"

        // Argument Constraints
        def jqlQuery = [
            jql   : "project = ${project.key} AND issuetype = '${JiraUseCase.IssueTypes.DOCUMENT_CHAPTER}' AND labels = LeVA_Doc:${documentType}",
            expand: ["names", "renderedFields"]
        ]

        // Stubbed Method Responses
        def jiraIssue1 = createJiraIssue("1")
        jiraIssue1.fields["0"] = "1.0"
        jiraIssue1.renderedFields = [:]
        jiraIssue1.renderedFields["1"] = "<html>myContent1</html>"
        jiraIssue1.renderedFields.description = "<html>1-description</html>"

        def jiraIssue2 = createJiraIssue("2")
        jiraIssue2.fields["0"] = "2.0"
        jiraIssue2.renderedFields = [:]
        jiraIssue2.renderedFields["1"] = "<html>myContent2</html>"
        jiraIssue2.renderedFields.description = "<html>2-description</html>"

        def jiraIssue3 = createJiraIssue("3")
        jiraIssue3.fields["0"] = "3.0"
        jiraIssue3.renderedFields = [:]
        jiraIssue3.renderedFields["1"] = "<html>myContent3</html>"
        jiraIssue3.renderedFields.description = "<html>3-description</html>"

        def jiraResult = [
            issues: [jiraIssue1, jiraIssue2, jiraIssue3],
            names : [
                "0": JiraUseCase.CustomIssueFields.HEADING_NUMBER,
                "1": JiraUseCase.CustomIssueFields.CONTENT
            ]
        ]

        when:
        def result = usecase.getDocumentChapterData(documentType)

        then:
        1 * jira.searchByJQLQuery(jqlQuery) >> jiraResult

        then:
        def expected = [
            "sec1s0": [
                number : "1.0",
                heading: "1-summary",
                content: "<html>myContent1</html>"
            ],
            "sec2s0": [
                number : "2.0",
                heading: "2-summary",
                content: "<html>myContent2</html>"
            ],
            "sec3s0": [
                number : "3.0",
                heading: "3-summary",
                content: "<html>myContent3</html>"
            ]
        ]

        result == expected
    }

    // TODO configure mocked JIRA for the test specification
    @Ignore
    def "handle with a meaningful error when jquery returns 0 JIRA issues as a response"() {
        given:
        // Test Parameters
        def documentType = "myDocumentType"

        when:
        def jiraResult = [
            "total"     : 0,
            "maxResults": 1000,
            "issues"    : []
        ]

        then:
        // Expect an error
        def msg = shouldFail IllegalStateException, {
            usecase.getDocumentChapterData(documentType)
        }
        assert msg.contains('No documents found') && msg.contains("JIRA")

    }

    def "match Jira test issues against test results"() {
        given:
        def testIssues = createJiraTestIssues()
        def testResults = createTestResults()

        def matched = [:]
        def matchedHandler = { result ->
            matched = result.collectEntries { jiraTestIssue, testcase ->
                [(jiraTestIssue.key.toString()), testcase.name]
            }
        }

        def mismatched = [:]
        def mismatchedHandler = { result ->
            mismatched = result.collect { it.key }
        }

        when:
        usecase.matchTestIssuesAgainstTestResults(testIssues, testResults, matchedHandler, mismatchedHandler)

        then:
        def expectedMatched = [
            "JIRA-1": "JIRA1_my-testcase-1",
            "JIRA-2": "JIRA2_my-testcase-2",
            "JIRA-3": "JIRA3_my-testcase-3",
            "JIRA-4": "JIRA4_my-testcase-4"
        ]

        def expectedMismatched = [
            "JIRA-5"
        ]

        matched == expectedMatched
        mismatched == expectedMismatched
    }

    def "report test results for component in DEV"() {
        given:

        def support = Mock(JiraUseCaseSupport)

        // Test Parameters
        def componentName = "myComponent"
        def testTypes = ["myTestType"]
        def testResults = createTestResults()

        // Stubbed Method Responses
        def testIssues = createJiraTestIssues()

        when:
        project = Spy(new FakeProject(this.steps)).init().load(git, jira)
        project.buildParams.targetEnvironmentToken = "D"
        JiraUseCase usecase = new JiraUseCase(project, steps, util, jira)
        usecase.setSupport(support)

        usecase.reportTestResultsForComponent(componentName, testTypes, testResults)

        then:
        1 * project.getAutomatedTests(componentName, testTypes) >> testIssues

        then:
        1 * support.applyXunitTestResults(testIssues, testResults)
        1 * util.warnBuildIfTestResultsContainFailure(testResults)
    }

    def "report test results for component with unexecuted Jira tests"() {
        given:
        def support = Mock(JiraUseCaseSupport)
        usecase.setSupport(support)

        // Test Parameters
        def componentName = "myComponent"
        def testTypes = ["myTestType"]
        def testResults = [:] // unexecuted tests

        // Stubbed Method Responses
        def testIssues = createJiraTestIssues()

        when:
        usecase.reportTestResultsForComponent(componentName, testTypes, testResults)

        then:
        1 * project.getAutomatedTests(componentName, testTypes) >> testIssues

        then:
        1 * util.warnBuildAboutUnexecutedJiraTests(testIssues)
    }

    def "report test results for component in QA"() {
        given:
        project.buildParams.targetEnvironmentToken = "Q"

        def support = Mock(JiraUseCaseSupport)
        usecase.setSupport(support)

        // Test Parameters
        def componentName = "myComponent"
        def testTypes = ["myTestType"]
        def testResults = createTestResults()

        // Argument Constraints
        def error = createTestResultErrors().first()
        def failure = createTestResultFailures().first()

        // Stubbed Method Responses
        def testIssues = createJiraTestIssues()
        def errorBug = [key: "JIRA-BUG-1"]
        def failureBug = [key: "JIRA-BUG-2"]

        when:
        usecase.reportTestResultsForComponent(componentName, testTypes, testResults)

        then:
        1 * project.getAutomatedTests(componentName, testTypes) >> testIssues

        then:
        1 * support.applyXunitTestResults(testIssues, testResults)
        1 * util.warnBuildIfTestResultsContainFailure(testResults)

        // create bug and block impacted test cases for error
        then:
        1 * jira.createIssueTypeBug(project.key, error.type, error.text) >> errorBug

        then:
        1 * jira.createIssueLinkTypeBlocks(errorBug, {
            // the Jira issue that shall be linked to the bug
            it.key == "JIRA-2"
        })

        then:
        1 * jira.appendCommentToIssue(errorBug.key, _)

        // create bug and block impacted test cases for failure
        then:
        1 * jira.createIssueTypeBug(project.key, failure.type, failure.text) >> failureBug

        then:
        1 * jira.createIssueLinkTypeBlocks(failureBug, {
            // the Jira issue that shall be linked to the bug
            it.key == "JIRA-3"
        })

        then:
        1 * jira.appendCommentToIssue(failureBug.key, _)
    }

    def "report test results for component in PROD"() {
        given:
        project.buildParams.targetEnvironmentToken = "P"

        def support = Mock(JiraUseCaseSupport)
        usecase.setSupport(support)

        // Test Parameters
        def componentName = "myComponent"
        def testTypes = ["myTestType"]
        def testResults = createTestResults()

        // Argument Constraints
        def testIssues = createJiraTestIssues()
        def error = createTestResultErrors().first()
        def failure = createTestResultFailures().first()

        // Stubbed Method Responses
        def errorBug = [key: "JIRA-BUG-1"]
        def failureBug = [key: "JIRA-BUG-2"]

        when:
        usecase.reportTestResultsForComponent(componentName, testTypes, testResults)

        then:
        1 * project.getAutomatedTests(componentName, testTypes) >> testIssues

        then:
        1 * support.applyXunitTestResults(testIssues, testResults)
        1 * util.warnBuildIfTestResultsContainFailure(testResults)

        // create bug and block impacted test cases for error
        then:
        1 * jira.createIssueTypeBug(project.key, error.type, error.text) >> errorBug

        then:
        1 * jira.createIssueLinkTypeBlocks(errorBug, {
            // the Jira issue that shall be linked to the bug
            it.key == "JIRA-2"
        })

        then:
        1 * jira.appendCommentToIssue(errorBug.key, _)

        // create bug and block impacted test cases for failure
        then:
        1 * jira.createIssueTypeBug(project.key, failure.type, failure.text) >> failureBug

        then:
        1 * jira.createIssueLinkTypeBlocks(failureBug, {
            // the Jira issue that shall be linked to the bug
            it.key == "JIRA-3"
        })

        then:
        1 * jira.appendCommentToIssue(failureBug.key, _)
    }

    def "walk test issues and test results"() {
        given:
        def testIssues = createJiraTestIssues()
        def testResults = createTestResults()

        def result = [:]
        def visitor = { jiraTestIssue, testcase, isMatch ->
            if (isMatch) result[jiraTestIssue.key] = testcase.name
        }

        when:
        usecase.walkTestIssuesAndTestResults(testIssues, testResults, visitor)

        then:
        def expected = [
            "JIRA-1": "JIRA1_my-testcase-1",
            "JIRA-2": "JIRA2_my-testcase-2",
            "JIRA-3": "JIRA3_my-testcase-3",
            "JIRA-4": "JIRA4_my-testcase-4"
        ]

        result == expected
    }
}
