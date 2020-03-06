package util

import groovy.json.JsonSlurper
import groovy.transform.InheritConstructors

import org.apache.http.client.utils.URIBuilder
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.ods.parser.JUnitParser
import org.ods.service.JiraService
import org.ods.util.GitUtil
import org.ods.util.IPipelineSteps
import org.ods.util.Project

import util.*

@InheritConstructors
class FakeGitUtil extends GitUtil {
    String getCommit() {
        return "my-commit"
    }

    String getURL() {
        return "https://github.com/my-org/my-repo-A.git"
    }
}

@InheritConstructors
class FakeProject extends Project {

    @Override
    Project load() {
        this.data.build = [:]
        this.data.build.hasFailingTests = false

        this.data.buildParams = loadBuildParams(steps)
        this.data.git = [commit: git.getCommit(), url: git.getURL() ]
        this.data.metadata = this.loadMetadata(METADATA_FILE_NAME)
        this.data.jira = this.convertJiraDataToJiraDataItems(this.loadJiraData(this.data.metadata.id))
        this.data.jiraResolved = this.resolveJiraDataItemReferences(this.data.jira)

        return this
    }

    static List<String> getBuildEnvironment(IPipelineSteps steps, boolean debug) {
        def env = new EnvironmentVariables()
        return FixtureHelper.createProjectBuildEnvironment(env)
    }

    protected URI getGitURLFromPath(String path, String remote) {
        def url = "https://github.com/my-org/my-repo-A.git"
        return new URIBuilder(url).build()
    }

    private File getResource(String path) {
        path = path.startsWith('/') ? path : '/' + path
        new File(getClass().getResource(path).toURI())
    }

    static Map loadBuildParams(IPipelineSteps steps) {
        return FixtureHelper.createProjectBuildParams()
    }

    protected Map loadJiraData(String projectKey) {
        def file = this.getResource("project-jira-data.json")
        return new JsonSlurper().parse(file)
    }

    protected Map loadMetadata(String filename) {
        return FixtureHelper.createProjectMetadata()
    }

    void setRepositories(List repos) {
        this.data.metadata.repositories = repos
    }
}

class FixtureHelper {
    static Project createProject() {
        def steps = new PipelineSteps()
        def git = new FakeGitUtil(steps)
        return new FakeProject(steps, git).load()
    }

    static Map createProjectBuildEnvironment(def env) {
        def params = createProjectBuildParams()
        params.each { key, value ->
            env.set(key, value)
        }

        return params
    }

    static Map createProjectBuildParams() {
        return [
            changeDescription: "The change I've wanted.",
            changeId: "0815",
            configItem: "myItem",
            sourceEnvironmentToClone: "dev",
            sourceEnvironmentToCloneToken: "D",
            targetEnvironment: "dev",
            targetEnvironmentToken: "D",
            version: "0.1"
        ]
    }

    static Map createProjectMetadata() {
        def result = [
                id          : "pltfmdev",
                name        : "Sock Shop",
                description : "A socks-selling e-commerce demo application.",
                services    : [
                        bitbucket: [
                                credentials: [
                                        id: "pltfmdev-cd-cd-user-with-password"
                                ]
                        ],
                        jira     : [
                                credentials: [
                                        id: "pltfmdev-cd-cd-user-with-password"
                                ]
                        ],
                        nexus    : [
                                repository: [
                                        name: "leva-documentation"
                                ]
                        ]
                ],
                repositories: [
                        [
                                id  : "demo-app-carts",
                                type: "ods-service",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-catalogue",
                                type: "ods",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-front-end",
                                type: "ods",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-test",
                                type: "ods-test",
                                data: [
                                        documents: [:]
                                ]
                        ]
                ],
                capabilities: []
        ]

        result.repositories.each { repo ->
            repo.data?.git = [
                    branch: "origin/master",
                    commit: UUID.randomUUID().toString().replaceAll("-", ""),
                    previousCommit: UUID.randomUUID().toString().replaceAll("-", ""),
                    previousSucessfulCommit: UUID.randomUUID().toString().replaceAll("-", ""),
                    url: "https://cd_user@somescm.com/scm/someproject/${repo.id}.git"
            ]
            repo.metadata = [
                    name: "Sock Shop: ${repo.id}",
                    description: "Some description for ${repo.id}",
                    supplier: "https://github.com/microservices-demo/",
                    version: "1.0"
            ]
        }

        return result
    }

    static Map createJiraIssue(String id, String issuetype = "Story", String summary = null, String description = null) {
        def result = [
            id: id,
            key: "JIRA-${id}",
            fields: [:],
            self: "http://${id}"
        ]

        result.fields.summary = summary ?: "${id}-summary"
        result.fields.description = description ?: "${id}-description"

        result.fields.components = []
        result.fields.issuelinks = []
        result.fields.issuetype = [
            name: issuetype
        ]

        return result
    }

    static Map createJiraIssueLink(String id, Map inwardIssue = null, Map outwardIssue = null) {
        def result = [
            id: id,
            type: [
                name: "Relate",
                inward: "relates to",
                outward: "is related to"
            ],
            self: "http://${id}"
        ]

        if (inwardIssue) {
            result.inwardIssue = inwardIssue
        }

        if (outwardIssue) {
            result.outwardIssue = outwardIssue
        }

        if (!inwardIssue && !outwardIssue) {
            result.outwardIssue = result.inwardIssue = createIssue(id)
        }

        return result
    }

    static List createJiraIssues(def issuetype = "Story") {
        def result = []

        // Create an issue belonging to 3 components and 2 inward links
        def issue1 = createJiraIssue("1", issuetype)
        issue1.fields.components = [
            [ name: "myComponentA" ],
            [ name: "myComponentB" ],
            [ name: "myComponentC" ]
        ]
        issue1.fields.issuelinks = [
            createJiraIssueLink("1", createJiraIssue("100")),
            createJiraIssueLink("2", createJiraIssue("101"))
        ]
        result << issue1

        // Create an issue belonging to 2 components and 1 outward links
        def issue2 = createJiraIssue("2", issuetype)
        issue2.fields.components = [
            [ name: "myComponentA" ],
            [ name: "myComponentB" ]
        ]
        issue2.fields.issuelinks = [
            createJiraIssueLink("1", createJiraIssue("200"))
        ]
        result << issue2

        // Create an issue belonging to 1 component and 0 outward links
        def issue3 = createJiraIssue("3", issuetype)
        issue3.fields.components = [
            [ name: "myComponentA" ]
        ]
        result << issue3

        // Create an issue belonging to 0 components and 0 outward links
        result << createJiraIssue("4")
    }

    static List createJiraDocumentIssues() {
        def result = []

        result << createJiraIssue("1", "my-doc-A", "Document A")
        result << createJiraIssue("2", "my-doc-B", "Document B")
        result << createJiraIssue("3", "my-doc-C", "Document C")

        return result
    }

    static List createJiraTestIssues() {
        def result = createJiraIssues("Test")

        def issue1 = result[0]
        issue1.fields.issuelinks = [
            createJiraIssueLink("1", null, createJiraIssue("100"))
        ]
        issue1.test = [
            description: issue1.description
        ]

        def issue2 = result[1]
        issue2.fields.issuelinks = [
            createJiraIssueLink("1", null, createJiraIssue("200")),
        ]
        issue2.test = [
            description: issue2.description
        ]

        def issue3 = result[2]
        issue3.fields.issuelinks = [
            createJiraIssueLink("1", null, createJiraIssue("300"))
        ]
        issue3.test = [
            description: issue3.description
        ]

        def issue4 = result[3]
        issue4.fields.issuelinks = [
            createJiraIssueLink("1", null, createJiraIssue("400")),
        ]
        issue4.test = [
            description: issue4.description
        ]

        def issue5 = createJiraIssue("5", "Test")
        issue5.fields.issuelinks = [
            createJiraIssueLink("1", null, createJiraIssue("500")),
        ]
        issue5.test = [
            description: issue5.description
        ]
        result << issue5

        return result
    }

    static String createJUnitXMLTestResults() {
        return """
        <testsuites name="sockshop-suites" tests="4" failures="1" errors="1">
            <testsuite name="sockshop-suite-1" tests="2" failures="0" errors="1" skipped="0">
                <properties>
                    <property name="my-property-a" value="my-property-a-value"/>
                </properties>
                <testcase name="PLTFMDEV401_verify-database-setup" classname="org.sockshop.DatabaseSetupTest" status="Succeeded" time="1"/>
                <testcase name="PLTFMDEV1060_verfify-database-installation" classname="org.sockshop.DatabaseInstallationTest" status="Error" time="2">
                    <error type="my-error-type" message="my-error-message">This is an error.</error>
                </testcase>
            </testsuite>
            <testsuite name="sockshop-suite-2" tests="2" failures="1" errors="0" skipped="1">
                <testcase name="PLTFMDEV1061_verify-database-is-operational" classname="org.sockshop.DatabaseOperationalTest" status="Failed" time="3">
                    <failure type="my-failure-type" message="my-failure-message">This is a failure.</failure>
                </testcase>
                <testcase name="PLTFMDEV1062_verify-databse-authentication" classname="org.sockshop.DatabaseAuthenticationTest" status="Missing" time="4">
                    <skipped/>
                </testcase>
            </testsuite>
            <testsuite name="sockshop-suite-3" tests="1" failures="0" errors="0" skipped="0">
                <testcase name="PLTFMDEV1046_verify-frontend-is-setup-correctly" classname="org.sockshop.FrontendSetupTest" status="Succeeded" time="5"/>
            </testsuite>
        </testsuites>
        """
    }

    static String createSockShopJUnitXmlTestResults() {
        // TODO  Sock Shop based test results
    }

    static Map createOpenShiftPodDataForComponent() {
        return [
            items: [
                [
                    metadata: [
                        name: "myPodName",
                        namespace: "myPodNamespace",
                        creationTimestamp: "myPodCreationTimestamp",
                        labels: [
                            env: "myPodEnvironment"
                        ]
                    ],
                    spec: [
                        nodeName: "myPodNode"
                    ],
                    status: [
                        podIP: "1.2.3.4",
                        phase: "myPodStatus"
                    ]
                ]
            ]
        ]
    }

    static Map createTestResults() {
        return JUnitParser.parseJUnitXML(
            createJUnitXMLTestResults()
        )
    }

    static Set createTestResultErrors() {
        return JUnitParser.Helper.getErrors(createTestResults())
    }

    static Set createTestResultFailures() {
        return JUnitParser.Helper.getFailures(createTestResults())
    }
}
