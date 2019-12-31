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
}
