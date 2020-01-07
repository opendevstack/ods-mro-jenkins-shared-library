package org.ods.service

import com.github.tomakehurst.wiremock.client.*

import groovy.json.JsonOutput

import org.apache.http.client.utils.URIBuilder

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class JiraZephyrServiceSpec extends SpecHelper {

    JiraZephyrService createService(int port, String username, String password) {
        return new JiraZephyrService("http://localhost:${port}", username, password)
    }

    Map getStepsFromIssueRequestData(Map mixins = [:]) {
        def result = [
            data: [
                issueId: "25140"
            ],
            headers: [
                "Accept": "application/json",
                "Content-Type": "application/json"
            ],
            password: "password",
            username: "username"
        ]

        result.path = "/rest/zapi/latest/teststep/${result.data.issueId}"

        return result << mixins
    }

    Map getStepsFromIssueResponseData(Map mixins = [:]) {
        def result = [
            body: JsonOutput.toJson([
                stepBeanCollection: [
                    [
                            data: 'Data1',
                            htmlResult: '<p>Result1</p>',
                            orderId:1,
                            customFields:[:],
                            attachmentsMap:[],
                            htmlData: '<p>Data1</p>',
                            result : 'Result1',
                            customFieldValuesMap:[:],
                            htmlStep: '<p>Step1</p>',
                            createdBy: 'user1@mail.com',
                            step: 'Step1',
                            modifiedBy: 'user1@mail.com',
                            id: 201,
                            totalStepCount:2
                    ],
                    [
                            data: 'Data2',
                            htmlResult: '<p>Result2</p>',
                            orderId:2,
                            customFields:[:],
                            attachmentsMap:[],
                            htmlData: '<p>Data2</p>',
                            result : 'Result2',
                            customFieldValuesMap:[:],
                            htmlStep: '<p>Step2</p>',
                            createdBy: 'user2@mail.com',
                            step: 'Step2',
                            modifiedBy: 'user2@mail.com',
                            id: 202,
                            totalStepCount:2
                    ]
                ]
            ])
        ]

        return result << mixins
    }

    def "get steps from issue with invalid issue id"() {
        given:
        def request = getStepsFromIssueRequestData()
        def response = getStepsFromIssueResponseData()

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getStepsFromIssue(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get steps from Jira issue. 'issueId' is undefined."

        cleanup:
        stopServer(server)
    }

    def "get steps from issue"() {
        given:
        def request = getStepsFromIssueRequestData()
        def response = getStepsFromIssueResponseData()

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getStepsFromIssue("25140")

        then:
        def expect = getStepsFromIssueResponseData()

        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "get steps from issue with HTTP 404 failure"() {
        given:
        def request = getStepsFromIssueRequestData()
        def response = getStepsFromIssueResponseData([
            status: 404
        ])

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getStepsFromIssue("25140")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to get steps from Jira issue. Jira could not be found at: 'http://localhost:${server.port()}'."

        cleanup:
        stopServer(server)
    }

    def "get steps from issue with HTTP 500 failure"() {
        given:
        def request = getStepsFromIssueRequestData()
        def response = getStepsFromIssueResponseData([
            status: 500,
            body: "Sorry, doesn't work!"
        ])

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getStepsFromIssue("25140")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to get steps from Jira issue. Jira responded with code: '${response.status}' and message: 'Sorry, doesn\'t work!'."

        cleanup:
        stopServer(server)
    }

    Map getProjectInfoRequestData(Map mixins = [:]) {
        def result = [
            data: [
                projectKey: "DEMO"
            ],
            headers: [
                "Accept": "application/json",
                "Content-Type": "application/json"
            ],
            password: "password",
            username: "username"
        ]

        result.path = "/rest/api/2/project/${result.data.projectKey}"

        return result << mixins
    }

    Map getProjectInfoResponseData(Map mixins = [:]) {
        def result = [
            body: JsonOutput.toJson([
                id: '12005',
                key: 'DEMO'
            ])
        ]

        return result << mixins
    }

    def "get info from project key with invalid project id (key)"() {
        given:
        def request = getProjectInfoRequestData()
        def response = getProjectInfoResponseData()

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getProjectInfo()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get project info from Jira. 'projectId' is undefined."

        cleanup:
        stopServer(server)
    }

    def "get info from project key"() {
        given:
        def request = getProjectInfoRequestData()
        def response = getProjectInfoResponseData()

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getProjectInfo("DEMO")

        then:
        def expect = getProjectInfoResponseData()

        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "get info from project key with HTTP 404 failure"() {
        given:
        def request = getProjectInfoRequestData()
        def response = getProjectInfoResponseData([
            status: 404
        ])

        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getProjectInfo("DEMO")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to get project info. Jira could not be found at: 'http://localhost:${server.port()}'."

        cleanup:
        stopServer(server)
    }

    def "get info from project key with HTTP 500 failure"() {
        given:
        def request = getProjectInfoRequestData()
        def response = getProjectInfoResponseData([
            status: 500,
            body: "Sorry, doesn't work!"
        ])


        def server = createServer(WireMock.&get, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.getProjectInfo("DEMO")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to get project info. Jira responded with code: '${response.status}' and message: 'Sorry, doesn\'t work!'."

        cleanup:
        stopServer(server)
    }

    Map createNewExecutionRequestData(Map mixins = [:]) {
        def result = [
            data: [
                issueId: '1234',
                projectId: '2345'
            ],
            headers: [
                "Accept": "application/json",
                "Content-Type": "application/json"
            ],
            password: "password",
            username: "username"
        ]

        result.body = JsonOutput.toJson([
            issueId: "${result.data.issueId}",
            projectId: "${result.data.projectId}"
        ])

        result.path = "/rest/zapi/latest/execution/"

        return result << mixins
    }

    Map createNewExecutionResponseData(Map mixins = [:]) {
        def result = [
            status: 200,
            body: JsonOutput.toJson([
                "13377": [
                    id: "13377",
                    orderId: "13377",
                    executionStatus: "-1",
                    comment: "",
                    htmlComment: "",
                    cycleId: -1,
                    cycleName: "Ad hoc",
                    versionId: 10001,
                    versionName: "Version2",
                    projectId: 2345,
                    createdBy: "vm_admin",
                    modifiedBy: "vm_admin",
                    assignedTo: "user1",
                    assignedToDisplay: "user1",
                    assignedToUserName: "user1",
                    assigneeType: "assignee",
                    issueId: 1234,
                    issueKey: "SAM-1234",
                    summary: "Test",
                    label: "",
                    component: "",
                    projectKey: "SAM",
                    folderId: 233,
                    folderName: "testfolder"
                ]
            ])
        ]

        return result << mixins
    }

    def "create new execution with invalid issue id"() {
        given:
        def request = createNewExecutionRequestData()
        def response = createNewExecutionResponseData()

        def server = createServer(WireMock.&post, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.createNewExecution(null, "2345")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to create new test execution from Jira issue. 'issueId' is undefined."

        cleanup:
        stopServer(server)
    }

    def "create new execution with invalid project id"() {
        given:
        def request = createNewExecutionRequestData()
        def response = createNewExecutionResponseData()

        def server = createServer(WireMock.&post, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.createNewExecution("1234", null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to create new test execution from Jira issue. 'projectId' is undefined."

        cleanup:
        stopServer(server)
    }

    def "create new execution"() {
        given:
        def request = createNewExecutionRequestData()
        def response = createNewExecutionResponseData()

        def server = createServer(WireMock.&post, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.createNewExecution("1234", "2345")

        then:
        def expect = createNewExecutionResponseData()

        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "create new execution with HTTP 404 failure"() {
        given:
        def request = createNewExecutionRequestData()
        def response = createNewExecutionResponseData([
            status: 404
        ])

        def server = createServer(WireMock.&post, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.createNewExecution("1234", "2345")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to create Jira new test execution. Jira could not be found at: 'http://localhost:${server.port()}'."

        cleanup:
        stopServer(server)
    }

    def "create new execution with HTTP 500 failure"() {
        given:
        def request = createNewExecutionRequestData()
        def response = createNewExecutionResponseData([
            status: 500,
            body: "Sorry, doesn't work!",
        ])

        def server = createServer(WireMock.&post, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.createNewExecution("1234", "2345")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to create Jira new test execution. Jira responded with code: '${response.status}' and message: 'Sorry, doesn\'t work!'."

        cleanup:
        stopServer(server)
    }


    Map updateExecutionRequestData(String status, Map mixins = [:]) {
        def result = [
            data: [
                executionId: "123456",
                status: status
            ],
            headers: [
                "Accept": "application/json",
                "Content-Type": "application/json"
            ],
            password: "password",
            username: "username"
        ]

        result.body = JsonOutput.toJson([
            status: "${result.data.status}"
        ])

        result.path = "/rest/zapi/latest/execution/${result.data.executionId}/execute"

        return result << mixins
    }

    Map updateExecutionResponseData(String status, Map mixins = [:]) {
        def result = [
            status: 200,
            body: JsonOutput.toJson([
                id: "123456",
                orderId: "123456",
                executionStatus: status,
                comment: "",
                htmlComment: "",
                cycleId: -1,
                cycleName: "Ad hoc",
                versionId: 10001,
                versionName: "Version2",
                projectId: 2345,
                createdBy: "vm_admin",
                modifiedBy: "vm_admin",
                assignedTo: "user1",
                assignedToDisplay: "user1",
                assignedToUserName: "user1",
                assigneeType: "assignee",
                issueId: 1234,
                issueKey: "SAM-1234",
                summary: "Test",
                label: "",
                component: "",
                projectKey: "SAM",
                folderId: 233,
                folderName: "testfolder"
            ])
        ]

        return result << mixins
    }

    def "update execution - generic - with invalid execution id"() {
        given:
        def request = updateExecutionRequestData("-1")
        def response = updateExecutionResponseData("-1")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecution(null, "-1")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to update test execution from Jira issue. 'executionId' is undefined."

        cleanup:
        stopServer(server)
    }

    def "update execution - generic - with invalid status"() {
        given:
        def request = updateExecutionRequestData("-1")
        def response = updateExecutionResponseData("-1")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecution("123456", null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to update test execution from Jira issue. 'status' is undefined."

        cleanup:
        stopServer(server)
    }

    def "update execution - generic"() {
        given:
        def request = updateExecutionRequestData("-1")
        def response = updateExecutionResponseData("-1")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecution("123456", "-1")

        then:
        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "update execution - generic - with HTTP 404 failure"() {
        given:
        def request = updateExecutionRequestData("-1")
        def response = updateExecutionResponseData("-1", [
            status: 404
        ])

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecution("123456", "-1")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to update Jira test execution. Jira could not be found at: 'http://localhost:${server.port()}'."

        cleanup:
        stopServer(server)
    }

    def "update execution - generic - with HTTP 500 failure"() {
        given:
        def request = updateExecutionRequestData("-1")
        def response = updateExecutionResponseData("-1", [
            status: 500,
            body: "Sorry, doesn't work!",
        ])

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecution("123456", "-1")

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to update Jira test execution. Jira responded with code: '${response.status}' and message: 'Sorry, doesn\'t work!'."

        cleanup:
        stopServer(server)
    }

    def "update execution - Pass"() {
        given:
        def request = updateExecutionRequestData("1")
        def response = updateExecutionResponseData("1")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecutionPass("123456")

        then:
        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "update execution - Fail"() {
        given:
        def request = updateExecutionRequestData("2")
        def response = updateExecutionResponseData("2")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecutionFail("123456")

        then:
        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "update execution - Wip"() {
        given:
        def request = updateExecutionRequestData("3")
        def response = updateExecutionResponseData("3")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecutionWip("123456")

        then:
        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

    def "update execution - Blocked"() {
        given:
        def request = updateExecutionRequestData("4")
        def response = updateExecutionResponseData("4")

        def server = createServer(WireMock.&put, request, response)
        def service = createService(server.port(), request.username, request.password)

        when:
        def result = service.updateExecutionBlocked("123456")

        then:
        noExceptionThrown()

        cleanup:
        stopServer(server)
    }

}
