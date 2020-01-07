package org.ods.usecase

class JiraUseCaseZephyrSupport extends AbstractJiraUseCaseSupport {

    JiraUseCaseZephyrSupport(JiraUseCase usecase) {
        super(usecase)
    }

    void applyTestResultsToAutomatedTestIssues(List jiraTestIssues, Map testResults) {
        jiraTestIssues.each { issue ->
            // Create a new execution (status UNEXECUTED)
            def execution = this.usecase.zephyr.createNewExecution(issue.id, issue.projectid)
            testResults.testsuites.each { testSuite ->
                testSuite.testcases.each { testCase ->
                    if(this.usecase.checkJiraIssueMatchesTestCase(issue, testCase.name)) {
                        if("Succeeded".equalsIgnoreCase(testCase.status)) {
                            this.usecase.zephyr.updateExecutionPass(execution.keySet().toArray()[0])
                        } else if ("Error".equalsIgnoreCase(testCase.status) || "Failed".equalsIgnoreCase(testCase.status)) {
                            this.usecase.zephyr.updateExecutionFail(execution.keySet().toArray()[0])
                        }
                    }
                }
            }
        }
    }

    List getAutomatedTestIssues(String projectId, String componentName = null, List<String> labelsSelector = []) {
        def info = this.usecase.zephyr.getProjectInfo(projectId)
        return super.getAutomatedTestIssues(projectId, componentName, labelsSelector).each { issue ->
            def steps = this.usecase.zephyr.getStepsFromIssue(issue.id)
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
