package org.ods.usecase

import org.ods.service.*

import org.ods.parser.JUnitParser

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class JiraUseCaseZephyrSupportSpec extends SpecHelper {

    JiraUseCase createUseCase(PipelineSteps steps, JiraService jira) {
        return new JiraUseCase(steps, jira)
    }

    JiraUseCaseZephyrSupport createUseCaseZephyrSupport(JiraUseCase usecase, JiraZephyrService zephyr) {
        return new JiraUseCaseZephyrSupport(usecase, zephyr)
    }

    def "apply test results to Jira issues - test case Passed"() {
        given:
        def steps = Spy(util.PipelineSteps)
        def jira = Mock(JiraService)
        def zephyr = Mock(JiraZephyrService)
        def usecase = createUseCase(steps, jira)
        def support = createUseCaseZephyrSupport(usecase, zephyr)
        
        def issuesList = [
            [id: '1', key: 'JIRA-1', projectid: '1234']
          ]
        def Map execution1 = ['123':[]]

        when:
        support.applyTestResultsToAutomatedTestIssues(issuesList, createTestResults())

        then:
        1 * zephyr.createNewExecution('1', '1234') >> execution1
        1 * zephyr.updateExecutionPass('123')
    }

    def "apply test results to Jira issues - test case Error"() {
        given:
        def steps = Spy(util.PipelineSteps)
        def jira = Mock(JiraService)
        def zephyr = Mock(JiraZephyrService)
        def usecase = createUseCase(steps, jira)
        def support = createUseCaseZephyrSupport(usecase, zephyr)

        def issuesList = [
            [id: '2', key: 'JIRA-2', projectid: '1234']
          ]
        def Map execution1 = ['123':[]]

        when:
        support.applyTestResultsToAutomatedTestIssues(issuesList, createTestResults())

        then:
        1 * zephyr.createNewExecution('2', '1234') >> execution1
        1 * zephyr.updateExecutionFail('123')
    }

    def "apply test results to Jira issues - test case Failed"() {
        given:
        def steps = Spy(util.PipelineSteps)
        def jira = Mock(JiraService)
        def zephyr = Mock(JiraZephyrService)
        def usecase = createUseCase(steps, jira)
        def support = createUseCaseZephyrSupport(usecase, zephyr)

        def issuesList = [
            [id: '3', key: 'JIRA-3', projectid: '1234']
          ]
        def Map execution1 = ['123':[]]

        when:
        support.applyTestResultsToAutomatedTestIssues(issuesList, createTestResults())

        then:
        1 * zephyr.createNewExecution('3', '1234') >> execution1
        1 * zephyr.updateExecutionFail('123')
    }

    def "apply test results to Jira issues - test case Missing"() {
        given:
        def steps = Spy(util.PipelineSteps)
        def jira = Mock(JiraService)
        def zephyr = Mock(JiraZephyrService)
        def usecase = createUseCase(steps, jira)
        def support = createUseCaseZephyrSupport(usecase, zephyr)

        def issuesList = [
            [id: '4', key: 'JIRA-4', projectid: '1234']
          ]
        def Map execution1 = ['123':[]]

        when:
        support.applyTestResultsToAutomatedTestIssues(issuesList, createTestResults())

        then:
        1 * zephyr.createNewExecution('4', '1234') >> execution1
        0 * zephyr.updateExecutionFail('123')
        0 * zephyr.updateExecutionPass('123')
    }

    def "get automated test issues for project - no results"() {
        given:
        def steps = Spy(util.PipelineSteps)
        def jira = Mock(JiraService)
        def zephyr = Mock(JiraZephyrService)
        def usecase = createUseCase(steps, jira)
        def support = createUseCaseZephyrSupport(usecase, zephyr)

        def jqlQuery = [
            jql: "project = PROJECT1 AND issuetype in ('Test') AND labels in ('AutomatedTest')",
            expand: [ "renderedFields" ],
            fields: [ "components", "description", "issuelinks", "issuetype", "summary" ]
        ]

        when:
        support.getAutomatedTestIssues("PROJECT1")

        then:
        1 * zephyr.getProjectInfo("PROJECT1")
        1 * jira.getIssuesForJQLQuery(jqlQuery)
    }
}
