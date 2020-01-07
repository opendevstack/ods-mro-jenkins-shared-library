package org.ods.service

@Grab(group="com.konghq", module="unirest-java", version="2.4.03", classifier="standalone")

import com.cloudbees.groovy.cps.NonCPS

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

import kong.unirest.Unirest

class JiraZephyrService extends JiraService {

    JiraZephyrService(String baseURL, String username, String password) {
        super(baseURL, username, password)
    }

    @NonCPS
    Map getStepsFromIssue(String issueId) {
        if (!issueId?.trim()) {
            throw new IllegalArgumentException("Error: unable to get steps from Jira issue. 'issueId' is undefined.")
        }

        def response = Unirest.get("${this.baseURL}/rest/zapi/latest/teststep/{issueId}")
            .routeParam("issueId", issueId)
            .basicAuth(this.username, this.password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asString()
        
        response.ifFailure {
            def message = "Error: unable to get steps from Jira issue. Jira responded with code: '${response.getStatus()}' and message: '${response.getBody()}'."

            if (response.getStatus() == 404) {
                message = "Error: unable to get steps from Jira issue. Jira could not be found at: '${this.baseURL}'."
            }

            throw new RuntimeException(message)
        }

        return new JsonSlurperClassic().parseText(response.getBody())
    }

    @NonCPS
    Map getProjectInfo(String projectId) {
        if (!projectId?.trim()) {
            throw new IllegalArgumentException("Error: unable to get project info from Jira. 'projectId' is undefined.")
        }

        def response = Unirest.get("${this.baseURL}/rest/api/2/project/{projectId}")
            .routeParam("projectId", projectId)
            .basicAuth(this.username, this.password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .asString()

        response.ifFailure {
            def message = "Error: unable to get project info. Jira responded with code: '${response.getStatus()}' and message: '${response.getBody()}'."

            if (response.getStatus() == 404) {
                message = "Error: unable to get project info. Jira could not be found at: '${this.baseURL}'. ${response.getBody()} "
            }

            throw new RuntimeException(message)
        }

        return new JsonSlurperClassic().parseText(response.getBody())
    }

    @NonCPS
    Map createNewExecution(String issueId, String projectId) {
        if (!issueId?.trim()) {
            throw new IllegalArgumentException("Error: unable to create new test execution from Jira issue. 'issueId' is undefined.")
        }
        if (!projectId?.trim()) {
            throw new IllegalArgumentException("Error: unable to create new test execution from Jira issue. 'projectId' is undefined.")
        }

        def response = Unirest.post("${this.baseURL}/rest/zapi/latest/execution/")
            .basicAuth(this.username, this.password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(JsonOutput.toJson(
                [
                    issueId: issueId,
                    projectId: projectId
                ]
            ))
            .asString()

        response.ifFailure {
            def message = "Error: unable to create Jira new test execution. Jira responded with code: '${response.getStatus()}' and message: '${response.getBody()}'."

            if (response.getStatus() == 404) {
                message = "Error: unable to create Jira new test execution. Jira could not be found at: '${this.baseURL}'. ${response.getBody()} "
            }

            throw new RuntimeException(message)
        }

        return new JsonSlurperClassic().parseText(response.getBody())
    }

    @NonCPS
    void updateExecution(String executionId, String status) {
        if (!executionId?.trim()) {
            throw new IllegalArgumentException("Error: unable to update test execution from Jira issue. 'executionId' is undefined.")
        }
        if (!status?.trim()) {
            throw new IllegalArgumentException("Error: unable to update test execution from Jira issue. 'status' is undefined.")
        }


        def response = Unirest.put("${this.baseURL}/rest/zapi/latest/execution/{executionId}/execute")
            .routeParam("executionId", executionId)
            .basicAuth(this.username, this.password)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(JsonOutput.toJson(
                [
                      status: status
                ]
            ))
            .asString()


        response.ifFailure {
            def message = "Error: unable to update Jira test execution. Jira responded with code: '${response.getStatus()}' and message: '${response.getBody()}'."

            if (response.getStatus() == 404) {
                message = "Error: unable to update Jira test execution. Jira could not be found at: '${this.baseURL}'. ${response.getBody()} "
            }

            throw new RuntimeException(message)
        }
    }

    void updateExecutionPass(String executionId) {
        this.updateExecution(executionId, "1")
    }

    void updateExecutionFail(String executionId) {
        this.updateExecution(executionId, "2")
    }

    void updateExecutionWip(String executionId) {
        this.updateExecution(executionId, "3")
    }

    void updateExecutionBlocked(String executionId) {
        this.updateExecution(executionId, "4")
    }

}
