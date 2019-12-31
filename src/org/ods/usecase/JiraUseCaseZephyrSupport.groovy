package org.ods.usecase

class JiraUseCaseZephyrSupport extends AbstractJiraUseCaseSupport {

    JiraUseCaseZephyrSupport(JiraUseCase usecase) {
        super(usecase)
    }

    void applyTestResultsToAutomatedTestIssues(List jiraTestIssues, Map testResults) {
    }

    List getAutomatedTestIssues(String projectId, String componentName = null, List<String> labelsSelector = []) {
        return super.getAutomatedTestIssues(projectId, componentName, labelsSelector).each { issue ->
            def steps = this.usecase.jira.getStepsFromIssue(issue.id)
            if(steps.stepBeanCollection){
                issue.test = 
                    steps.stepBeanCollection.collect { stepBean ->
                        return [step: stepBean.step, data: stepBean.data, result: stepBean.result]
                }
            }
        }
    }
}
