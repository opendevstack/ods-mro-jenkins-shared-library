package org.ods.usecase


import org.ods.parser.JUnitParser
import org.ods.service.JiraService
import org.ods.util.IPipelineSteps
import org.ods.util.MROPipelineUtil
import org.ods.util.Project
import org.ods.util.Project.JiraDataItem

class JiraUseCase {

    class IssueTypes {
        static final String DOCUMENT_CHAPTER = "Documentation Chapter"
    }

    class CustomIssueFields {
        static final String CONTENT = "Content"
        static final String HEADING_NUMBER = "Heading Number"
    }

    enum TestIssueLabels {
        Error,
        Failed,
        Missing,
        Skipped,
        Succeeded
    }

    private Project project
    private JiraService jira
    private IPipelineSteps steps
    private AbstractJiraUseCaseSupport support
    private MROPipelineUtil util

    JiraUseCase(Project project, IPipelineSteps steps, MROPipelineUtil util, JiraService jira) {
        this.project = project
        this.steps = steps
        this.util = util
        this.jira = jira
    }

    void setSupport(AbstractJiraUseCaseSupport support) {
        this.support = support
    }

    void applyXunitTestResultsAsTestIssueLabels(List testIssues, Map testResults) {
        if (!this.jira) return

        // Handle Jira test issues for which a corresponding test exists in testResults
        def matchedHandler = { result ->
            result.each { testIssue, testCase ->
                def issueLabels = [TestIssueLabels.Succeeded as String]
                if (testCase.skipped || testCase.error || testCase.failure) {
                    if (testCase.error) {
                        issueLabels = [TestIssueLabels.Error as String]
                    }

                    if (testCase.failure) {
                        issueLabels = [TestIssueLabels.Failed as String]
                    }

                    if (testCase.skipped) {
                        issueLabels = [TestIssueLabels.Skipped as String]
                    }
                }

                this.jira.removeLabelsFromIssue(testIssue.key, TestIssueLabels.values().collect { it.toString() })
                this.jira.addLabelsToIssue(testIssue.key, issueLabels)
            }
        }

        // Handle Jira test issues for which no corresponding test exists in testResults
        def unmatchedHandler = { result ->
            result.each { testIssue ->
                this.jira.removeLabelsFromIssue(testIssue.key, TestIssueLabels.values().collect { it.toString() })
                this.jira.addLabelsToIssue(testIssue.key, [TestIssueLabels.Missing as String])
            }
        }

        this.matchTestIssuesAgainstTestResults(testIssues, testResults, matchedHandler, unmatchedHandler)
    }

    boolean checkTestsIssueMatchesTestCase(Map testIssue, Map testCase) {
        // FIXME: the contents of this method have been duplicated below to allow the execution of tests
        def testIssueKeyClean = testIssue.key.replaceAll("-", "")
        return testCase.name.startsWith("${testIssueKeyClean} ") || testCase.name.startsWith("${testIssueKeyClean}-") || testCase.name.startsWith("${testIssueKeyClean}_")
    }

    private String convertHTMLImageSrcIntoBase64Data(String html) {
        def server = this.jira.baseURL

        def pattern = ~/src="(${server}.*\.(?:gif|jpg|jpeg|png))"/
        def result = html.replaceAll(pattern) { match ->
            def src = match[1]
            def img = this.jira.getFileFromJira(src)
            return "src=\"data:${img.contentType};base64,${img.data.encodeBase64()}\""
        }

        return result
    }

    void createBugsForFailedTestIssues(List testIssues, Set testFailures, String comment) {
        if (!this.jira) return

        testFailures.each { failure ->
            def bug = this.jira.createIssueTypeBug(this.project.key, failure.type, failure.text)


            // TODO how to map bugs and failures to test issues
            this.walkTestIssuesAndTestResults(testIssues, failure) { testIssue, testCase, isMatch ->
                if (isMatch) {
                    testIssue.bugs << bug.key

                    // add newly created bug into the Jira data structure
                    this.project.data.jira.bugs[bug.key] = new JiraDataItem(project, [
                        key     : bug.key,
                        name    : failure.type,
                        assignee: "Unassigned",
                        dueDate : "",
                        status  : "TO DO",
                        tests   : [testIssue.key]
                    ], Project.JiraDataItem.TYPE_BUGS)

                    // add newly created bug into the Jira data structure of resolved items
                    this.project.data.jiraResolved.bugs[bug.key] = this.project.data.jira.bugs[bug.key]
                    this.project.data.jiraResolved.bugs[bug.key].tests[0] = this.project.data.jira.tests[testIssue.key]

                    this.jira.createIssueLinkTypeBlocks(bug, testIssue)
                }
            }

            this.jira.appendCommentToIssue(bug.key, comment)
        }
    }

    Map getDocumentChapterData(String documentType) {
        if (!this.jira) return [:]

        def jiraDocumentChapterLabel = this.getDocumentChapterIssueLabelForDocumentType(documentType)

        def jqlQuery = [
            jql   : "project = ${this.project.key} AND issuetype = '${IssueTypes.DOCUMENT_CHAPTER}' AND labels = ${jiraDocumentChapterLabel}",
            expand: ["names", "renderedFields"]
        ]

        def result = this.jira.searchByJQLQuery(jqlQuery)
        // We should fail the document if no matching documentation issues are found
        if (!result || result.total == 0) throw new IllegalStateException("No documents found in JIRA for jqlQuery ${jqlQuery}")
        def numberKeys = result.names.findAll { it.value == CustomIssueFields.HEADING_NUMBER }.collect { it.key }
        def contentFieldKeys = result.names.findAll { it.value == CustomIssueFields.CONTENT }.collect { it.key }

        return result.issues.collectEntries { issue ->

            def number = issue.fields.find { field ->
                numberKeys.contains(field.key) && field.value
            }
            if (!number) {
                throw new IllegalArgumentException("Error: Could not find heading number for document ${documentType} and issue ${issue.key}.")
            }
            number = number.getValue().trim()

            def content = issue.renderedFields.find { field ->
                contentFieldKeys.contains(field.key) && field.value
            }
            content = content ? content.getValue() : ""

            if (content.contains("<img")) {
                content = this.convertHTMLImageSrcIntoBase64Data(content)
            }

            return [
                "sec${number.replaceAll(/\./, "s")}".toString(),
                [
                    number : number,
                    heading: issue.fields.summary,
                    content: content?.replaceAll("\u00a0", " ") ?: ""
                ]
            ]
        }
    }

    private String getDocumentChapterIssueLabelForDocumentType(String documentType) {
        return "LeVA_Doc:${documentType}"
    }

    void matchTestIssuesAgainstTestResults(List testIssues, Map testResults, Closure matchedHandler, Closure unmatchedHandler = null) {
        def result = [
            matched  : [:],
            unmatched: []
        ]

        this.walkTestIssuesAndTestResults(testIssues, testResults) { testIssue, testCase, isMatch ->
            if (isMatch) {
                result.matched << [
                    (testIssue): testCase
                ]
            }
        }

        testIssues.each { testIssue ->
            if (!result.matched.keySet().contains(testIssue)) {
                result.unmatched << testIssue
            }
        }

        if (matchedHandler) {
            matchedHandler(result.matched)
        }

        if (unmatchedHandler) {
            unmatchedHandler(result.unmatched)
        }
    }

    void reportTestResultsForComponent(String componentName, List<String> testTypes, Map testResults) {
        if (!this.jira) return

        def testIssues = this.project.getAutomatedTests(componentName, testTypes)

        this.util.warnBuildIfTestResultsContainFailure(testResults)
        this.matchTestIssuesAgainstTestResults(testIssues, testResults, null) { unexecutedJiraTests ->
            if (!unexecutedJiraTests.isEmpty()) {
                this.util.warnBuildAboutUnexecutedJiraTests(unexecutedJiraTests)
            }
        }

        this.support.applyXunitTestResults(testIssues, testResults)
        if (["D", "Q", "P"].contains(this.project.buildParams.targetEnvironmentToken)) {
            // Create bugs for erroneous test issues
            def errors = JUnitParser.Helper.getErrors(testResults)
            this.createBugsForFailedTestIssues(testIssues, errors, this.steps.env.RUN_DISPLAY_URL)

            // Create bugs for failed test issues
            def failures = JUnitParser.Helper.getFailures(testResults)
            this.createBugsForFailedTestIssues(testIssues, failures, this.steps.env.RUN_DISPLAY_URL)
        }
    }

    private void walkTestIssuesAndTestResults(List testIssues, Map testResults, Closure visitor) {
        testResults.testsuites.each { testSuite ->
            testSuite.testcases.each { testCase ->
                def testIssue = testIssues.find { testIssue ->
                    // FIXME: invoking checkTestsIssueMatchesTestCase results in failing tests (presumably be a bug in a test dependency)
                    // this.checkTestsIssueMatchesTestCase(testIssue, testCase)
                    def testIssueKeyClean = testIssue.key.replaceAll("-", "")
                    return testCase.name.startsWith("${testIssueKeyClean} ") || testCase.name.startsWith("${testIssueKeyClean}-") || testCase.name.startsWith("${testIssueKeyClean}_")
                }

                def isMatch = testIssue != null
                visitor(testIssue, testCase, isMatch)
            }
        }
    }
}
