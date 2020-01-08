package org.ods.usecase

abstract class AbstractJiraUseCaseSupport {

    protected JiraUseCase usecase

    AbstractJiraUseCaseSupport(JiraUseCase usecase) {
        this.usecase = usecase
    }

    abstract void applyTestResultsToAutomatedTestIssues(List jiraTestIssues, Map testResults)

    List getAutomatedTestIssues(String projectId, String componentName = null, List<String> labelsSelector = []) {
        if (!this.usecase.jira) return []

        return this.usecase.getIssuesForProject(projectId, componentName, ["Test"], labelsSelector << "AutomatedTest", false) { issuelink ->
            return issuelink.type.relation == "is related to" && (issuelink.issue.issuetype.name == "Epic" || issuelink.issue.issuetype.name == "Story")
        }.values().flatten()
    }

    List getUnitTestIssues(String projectId, String componentName = null) {
        return this.getAutomatedTestIssues(projectId, componentName, ["UnitTest"])
    }

    List getIntegrationTestIssues(String projectId, String componentName = null) {
        return this.getAutomatedTestIssues(projectId, componentName, ["IntegrationTest"])
    }

    List getAcceptanceTestIssues(String projectId, String componentName = null) {
        return this.getAutomatedTestIssues(projectId, componentName, ["AcceptanceTest"])
    }

    List getInstallationTestIssues(String projectId, String componentName = null) {
        return this.getAutomatedTestIssues(projectId, componentName, ["InstallationTest"])
    }
}
