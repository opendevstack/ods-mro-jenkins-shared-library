package org.ods.usecase

import org.ods.service.JiraZephyrService

class JiraUseCaseZephyrSupport extends AbstractJiraUseCaseSupport {

    private JiraZephyrService zephyr

    JiraUseCaseZephyrSupport(JiraUseCase usecase, JiraZephyrService zephyr) {
        super(usecase)
        this.zephyr = zephyr
    }

    void applyTestResultsToAutomatedTestIssues(List jiraTestIssues, Map testResults) {
        jiraTestIssues.each { issue ->
            // Create a new execution (status UNEXECUTED)
            def execution = this.zephyr.createNewExecution(issue.id, issue.projectid)
            testResults.testsuites.each { testSuite ->
                testSuite.testcases.each { testCase ->
                    if(this.usecase.checkJiraIssueMatchesTestCase(issue, testCase.name)) {
                        if("Succeeded".equalsIgnoreCase(testCase.status)) {
                            this.zephyr.updateExecutionPass(execution.keySet().toArray()[0])
                        } else if ("Error".equalsIgnoreCase(testCase.status) || "Failed".equalsIgnoreCase(testCase.status)) {
                            this.zephyr.updateExecutionFail(execution.keySet().toArray()[0])
                        }
                    }
                }
            }
        }
    }

    List getAutomatedTestIssues(String projectId, String componentName = null, List<String> labelsSelector = []) {
        def info = this.zephyr.getProjectInfo(projectId)
        return super.getAutomatedTestIssues(projectId, componentName, labelsSelector).each { issue ->
            def steps = this.zephyr.getStepsFromIssue(issue.id)
            if(steps.stepBeanCollection){
                issue.test = 
                    steps.stepBeanCollection.collect { stepBean ->
                        return [step: stepBean.step, data: stepBean.data, result: stepBean.result]
                }
            }
           // The project id (not key) is mandatory to generate new executions
           issue.projectid = info.id
        }
    }
}
