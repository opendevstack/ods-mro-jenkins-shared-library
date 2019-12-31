package org.ods.service

@Grab(group="com.konghq", module="unirest-java", version="2.4.03", classifier="standalone")

import com.cloudbees.groovy.cps.NonCPS

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
}
