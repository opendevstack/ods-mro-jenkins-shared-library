package org.ods.util

import org.ods.service.JiraService
import org.yaml.snakeyaml.Yaml
import util.SpecHelper

import java.nio.file.Paths

import static util.FixtureHelper.*

class ProjectSpec extends SpecHelper {

    GitUtil git
    JiraService jira
    File metadataFile
    Project project
    IPipelineSteps steps

    def setup() {
        steps = Spy(util.PipelineSteps)
        git = Mock(GitUtil)
        metadataFile = createProjectMetadataFile(this.steps.env.WORKSPACE, steps)
        jira = Mock(JiraService)

        project = Spy(constructorArgs: [steps], {
            loadJiraDataDocs() >> {
                return createProjectJiraDataDocs()
            }
        })
        project.init().load(git, jira)
    }

    def cleanup() {
        metadataFile.delete()
    }

    File createProjectMetadataFile(String path, IPipelineSteps steps) {
        def file = Paths.get(path, "metadata.yml").toFile()

        file << new Yaml().dump(createProjectMetadata())

        return file
    }

    def "get build environment for DEBUG"() {
        when:
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "DEBUG=false" }

        when:
        result = Project.getBuildEnvironment(steps, true)

        then:
        result.find { it == "DEBUG=true" }

        when:
        result = Project.getBuildEnvironment(steps, false)

        then:
        result.find { it == "DEBUG=false" }
    }

    def "get build environment for MULTI_REPO_BUILD"() {
        when:
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_BUILD=true" }
    }

    def "get build environment for MULTI_REPO_ENV"() {
        when:
        steps.env.environment = null
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV=dev" }

        when:
        steps.env.environment = ""
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV=dev" }

        when:
        steps.env.environment = "myEnv"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV=myEnv" }
    }

    def "get build environment for MULTI_REPO_ENV_TOKEN"() {
        when:
        steps.env.environment = "dev"
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV_TOKEN=D" }

        when:
        steps.env.environment = "qa"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV_TOKEN=Q" }

        when:
        steps.env.environment = "prod"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "MULTI_REPO_ENV_TOKEN=P" }
    }

    def "get build environment for RELEASE_PARAM_CHANGE_ID"() {
        when:
        steps.env.changeId = null
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_ID=0.1-myEnv" }

        when:
        steps.env.changeId = ""
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_ID=0.1-myEnv" }

        when:
        steps.env.changeId = "myId"
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_ID=myId" }
    }

    def "get build environment for RELEASE_PARAM_CHANGE_DESC"() {
        when:
        steps.env.changeDescription = null
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_DESC=UNDEFINED" }

        when:
        steps.env.changeDescription = ""
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_DESC=UNDEFINED" }

        when:
        steps.env.changeDescription = "myDescription"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CHANGE_DESC=myDescription" }
    }

    def "get build environment for RELEASE_PARAM_CONFIG_ITEM"() {
        when:
        steps.env.configItem = null
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CONFIG_ITEM=UNDEFINED" }

        when:
        steps.env.configItem = ""
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CONFIG_ITEM=UNDEFINED" }

        when:
        steps.env.configItem = "myItem"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_CONFIG_ITEM=myItem" }
    }

    def "get build environment for RELEASE_PARAM_VERSION"() {
        when:
        steps.env.version = null
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_VERSION=WIP" }

        when:
        steps.env.version = ""
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_VERSION=WIP" }

        when:
        steps.env.version = "0.1"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "RELEASE_PARAM_VERSION=0.1" }
    }

    def "get build environment for SOURCE_CLONE_ENV"() {
        when:
        steps.env.environment = "myEnv"
        steps.env.sourceEnvironmentToClone = null
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV=myEnv" }

        when:
        steps.env.environment = "myEnv"
        steps.env.sourceEnvironmentToClone = ""
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV=myEnv" }

        when:
        steps.env.environment = "mvEnv"
        steps.env.sourceEnvironmentToClone = "mySourceEnv"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV=mySourceEnv" }
    }

    def "get build environment for SOURCE_CLONE_ENV_TOKEN"() {
        when:
        steps.env.sourceEnvironmentToClone = "dev"
        def result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV_TOKEN=D" }

        when:
        steps.env.sourceEnvironmentToClone = "qa"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV_TOKEN=Q" }

        when:
        steps.env.sourceEnvironmentToClone = "prod"
        result = Project.getBuildEnvironment(steps)

        then:
        result.find { it == "SOURCE_CLONE_ENV_TOKEN=P" }
    }

    def "get Git URL from path"() {
        given:
        def path = "${steps.env.WORKSPACE}/a/b/c"
        def origin = "upstream"

        when:
        def result = project.getGitURLFromPath(path, origin)

        then:
        1 * steps.dir(path, _)

        then:
        1 * steps.sh({ it.script == "git config --get remote.${origin}.url" && it.returnStdout }) >> new URI("https://github.com/my-org/my-repo.git").toString()

        then:
        result == new URI("https://github.com/my-org/my-repo.git")

        cleanup:
        metadataFile.delete()
    }

    def "get Git URL from path without origin"() {
        given:
        def path = "${steps.env.WORKSPACE}/a/b/c"

        when:
        def result = project.getGitURLFromPath(path)

        then:
        1 * steps.dir(path, _)

        then:
        1 * steps.sh({ it.script == "git config --get remote.origin.url" && it.returnStdout }) >> new URI("https://github.com/my-org/my-repo.git").toString()

        then:
        result == new URI("https://github.com/my-org/my-repo.git")

        cleanup:
        metadataFile.delete()
    }

    def "get Git URL from path with invalid path"() {
        when:
        project.getGitURLFromPath(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get Git URL. 'path' is undefined."

        when:
        project.getGitURLFromPath("")

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get Git URL. 'path' is undefined."

        when:
        def path = "myPath"
        project.getGitURLFromPath(path)

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get Git URL. 'path' must be inside the Jenkins workspace: ${path}"

        cleanup:
        metadataFile.delete()
    }

    def "get Git URL from path with invalid remote"() {
        given:
        def path = "${steps.env.WORKSPACE}/a/b/c"

        when:
        project.getGitURLFromPath(path, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get Git URL. 'remote' is undefined."

        when:
        project.getGitURLFromPath(path, "")

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to get Git URL. 'remote' is undefined."

        cleanup:
        metadataFile.delete()
    }

    def "load"() {
        given:
        def component1 = [key: "CMP-1", name: "Component 1"]
        def epic1 = [key: "EPC-1", name: "Epic 1"]
        def mitigation1 = [key: "MTG-1", name: "Mitigation 1"]
        def requirement1 = [key: "REQ-1", name: "Requirement 1"]
        def risk1 = [key: "RSK-1", name: "Risk 1"]
        def techSpec1 = [key: "TS-1", name: "Technical Specification 1"]
        def test1 = [key: "TST-1", name: "Test 1"]
        def test2 = [key: "TST-2", name: "Test 2"]
        def doc1 = [key: "DOC-1", name: "Doc 1", status: "OPEN"]

        // Define key-based references
        component1.epics = [epic1.key]
        component1.mitigations = [mitigation1.key]
        component1.requirements = [requirement1.key]
        component1.risks = [risk1.key]
        component1.tests = [test1.key, test2.key]
        component1.techSpecs = [techSpec1.key]

        epic1.components = [component1.key]
        epic1.requirements = [requirement1.key]

        mitigation1.components = [component1.key]
        mitigation1.requirements = [requirement1.key]
        mitigation1.risks = [risk1.key]

        requirement1.components = [component1.key]
        requirement1.epics = [epic1.key]
        requirement1.mitigations = [mitigation1.key]
        requirement1.risks = [risk1.key]
        requirement1.tests = [test1.key, test2.key]

        risk1.components = [component1.key]
        risk1.requirements = [requirement1.key]
        risk1.tests = [test1.key, test2.key]

        techSpec1.components = [component1.key]

        test1.components = [component1.key]
        test1.requirements = [requirement1.key]
        test1.risks = [risk1.key]

        test2.components = [component1.key]
        test2.requirements = [requirement1.key]
        test2.risks = [risk1.key]



        when:
        project.load(this.git, this.jira)

        then:
        1 * project.loadJiraData(_) >> [
            project     : [name: "my-project"],
            bugs        : [],
            components  : [(component1.key): component1],
            epics       : [(epic1.key): epic1],
            mitigations : [(mitigation1.key): mitigation1],
            requirements: [(requirement1.key): requirement1],
            risks       : [(risk1.key): risk1],
            tests       : [(test1.key): test1, (test2.key): test2],
            techSpecs   : [(techSpec1.key): techSpec1],
            docs        : [(doc1.key): doc1]
        ]

        1 * project.resolveJiraDataItemReferences(_)
        1 * project.loadJiraDataDocs() >> createProjectJiraDataDocs()

        then:
        def components = project.components
        components.first() == component1

        // Unresolved references
        components.first().epics == [epic1.key]
        components.first().mitigations == [mitigation1.key]
        components.first().requirements == [requirement1.key]
        components.first().risks == [risk1.key]
        components.first().tests == [test1.key, test2.key]
        components.first().techSpecs == [techSpec1.key]

        // Resolved references
        components.first().getResolvedEpics() == [epic1]
        components.first().getResolvedMitigations() == [mitigation1]
        components.first().getResolvedSystemRequirements() == [requirement1]
        components.first().getResolvedRisks() == [risk1]
        components.first().getResolvedTests() == [test1, test2]
        components.first().getResolvedTechnicalSpecifications() == [techSpec1]

        // Resolved transitive references
        components.first().getResolvedEpics().first().getResolvedComponents() == [component1]
        components.first().getResolvedEpics().first().getResolvedSystemRequirements() == [requirement1]
        components.first().getResolvedEpics().first().getResolvedSystemRequirements().first().getResolvedMitigations() == [mitigation1]
        components.first().getResolvedEpics().first().getResolvedSystemRequirements().first().getResolvedTests() == [test1, test2]
        components.first().getResolvedEpics().first().getResolvedSystemRequirements().first().getResolvedTests().first().getResolvedRisks() == [risk1]
    }

    def "load build param changeDescription"() {
        when:
        steps.env.changeDescription = null
        def result = Project.loadBuildParams(steps)

        then:
        result.changeDescription == "UNDEFINED"

        when:
        steps.env.changeDescription = ""
        result = Project.loadBuildParams(steps)

        then:
        result.changeDescription == "UNDEFINED"

        when:
        steps.env.changeDescription = "myDescription"
        result = Project.loadBuildParams(steps)

        then:
        result.changeDescription == "myDescription"
    }

    def "load build param changeId"() {
        when:
        steps.env.changeId = null
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        def result = Project.loadBuildParams(steps)

        then:
        result.changeId == "0.1-myEnv"

        when:
        steps.env.changeId = ""
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        result = Project.loadBuildParams(steps)

        then:
        result.changeId == "0.1-myEnv"

        when:
        steps.env.changeId = "myId"
        steps.env.environment = "myEnv"
        steps.env.version = "0.1"
        result = Project.loadBuildParams(steps)

        then:
        result.changeId == "myId"
    }

    def "load build param configItem"() {
        when:
        steps.env.configItem = null
        def result = Project.loadBuildParams(steps)

        then:
        result.configItem == "UNDEFINED"

        when:
        steps.env.configItem = ""
        result = Project.loadBuildParams(steps)

        then:
        result.configItem == "UNDEFINED"

        when:
        steps.env.configItem = "myItem"
        result = Project.loadBuildParams(steps)

        then:
        result.configItem == "myItem"
    }

    def "load build param sourceEnvironmentToClone"() {
        when:
        steps.env.environment = "myEnv"
        steps.env.sourceEnvironmentToClone = null
        def result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToClone == "myEnv"

        when:
        steps.env.environment = "myEnv"
        steps.env.sourceEnvironmentToClone = ""
        result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToClone == "myEnv"

        when:
        steps.env.environment = "mvEnv"
        steps.env.sourceEnvironmentToClone = "mySourceEnv"
        result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToClone == "mySourceEnv"
    }

    def "load build param sourceEnvironmentToCloneToken"() {
        when:
        steps.env.sourceEnvironmentToClone = "dev"
        def result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToCloneToken == "D"

        when:
        steps.env.sourceEnvironmentToClone = "qa"
        result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToCloneToken == "Q"

        when:
        steps.env.sourceEnvironmentToClone = "prod"
        result = Project.loadBuildParams(steps)

        then:
        result.sourceEnvironmentToCloneToken == "P"
    }

    def "load build param targetEnvironment"() {
        when:
        steps.env.environment = null
        def result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironment == "dev"

        when:
        steps.env.environment = ""
        result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironment == "dev"

        when:
        steps.env.environment = "myEnv"
        result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironment == "myEnv"
    }

    def "load build param targetEnvironmentToken"() {
        when:
        steps.env.environment = "dev"
        def result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironmentToken == "D"

        when:
        steps.env.environment = "qa"
        result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironmentToken == "Q"

        when:
        steps.env.environment = "prod"
        result = Project.loadBuildParams(steps)

        then:
        result.targetEnvironmentToken == "P"
    }

    def "load build param version"() {
        when:
        steps.env.version = null
        def result = Project.loadBuildParams(steps)

        then:
        result.version == "WIP"

        when:
        steps.env.version = ""
        result = Project.loadBuildParams(steps)

        then:
        result.version == "WIP"

        when:
        steps.env.version = "0.1"
        result = Project.loadBuildParams(steps)

        then:
        result.version == "0.1"
    }

    def "load metadata"() {
        given:
        steps.sh(_) >> "https://github.com/my-org/my-pipeline-repo.git"

        when:
        def result = project.loadMetadata()

        then:
        def expected = [
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
                    id      : "demo-app-carts",
                    type    : "ods-service",
                    data    : [
                        "documents": [:]
                    ],
                    metadata: [
                        name       : "Sock Shop: demo-app-carts",
                        description: "Some description for demo-app-carts",
                        supplier   : "https://github.com/microservices-demo/",
                        version    : "1.0"
                    ],
                    url     : "https://github.com/my-org/pltfmdev-demo-app-carts.git",
                    branch  : "master"
                ],
                [
                    id      : "demo-app-catalogue",
                    type    : "ods",
                    data    : [
                        "documents": [:]
                    ],
                    metadata: [
                        supplier   : "https://github.com/microservices-demo/",
                        name       : "Sock Shop: demo-app-catalogue",
                        description: "Some description for demo-app-catalogue",
                        version    : "1.0"
                    ],
                    branch  : "master",
                    url     : "https://github.com/my-org/pltfmdev-demo-app-catalogue.git"
                ],
                [
                    "metadata": [
                        supplier   : "https://github.com/microservices-demo/",
                        name       : "Sock Shop: demo-app-front-end",
                        description: "Some description for demo-app-front-end",
                        version    : "1.0"
                    ],
                    data      : [
                        "documents": [:]
                    ],
                    id        : "demo-app-front-end",
                    type      : "ods",
                    branch    : "master",
                    url       : "https://github.com/my-org/pltfmdev-demo-app-front-end.git"
                ],
                [
                    metadata: [
                        supplier   : "https://github.com/microservices-demo/",
                        name       : "Sock Shop: demo-app-test",
                        description: "Some description for demo-app-test",
                        version    : "1.0"
                    ],
                    data    : [
                        documents: [:]
                    ],
                    id      : "demo-app-test",
                    type    : "ods-test",
                    branch  : "master",
                    url     : "https://github.com/my-org/pltfmdev-demo-app-test.git"
                ]
            ],
            capabilities: []
        ]

        result == expected
    }

    def "load metadata with invalid file"() {
        when:
        project.loadMetadata(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. 'filename' is undefined."
    }

    def "load project metadata with non-existent file"() {
        when:
        def filename = "non-existent"
        project.loadMetadata(filename)

        then:
        def e = thrown(RuntimeException)
        e.message == "Error: unable to load project meta data. File '${steps.env.WORKSPACE}/${filename}' does not exist."
    }

    def "load project metadata with invalid id"() {
        when:
        metadataFile.text = """
            name: myName
        """

        project.loadMetadata()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'id' is undefined."
    }

    def "load project metadata with invalid name"() {
        when:
        metadataFile.text = """
            id: myId
        """

        project.loadMetadata()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'name' is undefined."
    }

    def "load project metadata with invalid description"() {
        when:
        metadataFile.text = """
            id: myId
            name: myName
            repositories:
              - id: A
        """

        def result = project.loadMetadata()

        then:
        result.description == ""
    }

    def "load project metadata with undefined repositories"() {
        when:
        metadataFile.text = """
            id: myId
            name: myName
        """

        def result = project.loadMetadata()

        then:
        result.repositories == []
    }

    def "load project metadata with invalid repository id"() {
        when:
        metadataFile.text = """
            id: myId
            name: myName
            repositories:
              - name: A
        """

        project.loadMetadata()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'repositories[0].id' is undefined."

        when:
        metadataFile.text = """
            id: myId
            name: myName
            repositories:
              - id: A
              - name: B
        """

        project.loadMetadata()

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'repositories[1].id' is undefined."
    }

    def "load project metadata with invalid repository url"() {
        when:
        metadataFile.text = """
            id: myId
            name: myName
            repositories:
              - name: A
        """

        project.loadMetadata()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'repositories[0].id' is undefined."

        when:
        metadataFile.text = """
            id: myId
            name: myName
            repositories:
              - id: A
              - name: B
        """

        project.loadMetadata()

        then:
        e = thrown(IllegalArgumentException)
        e.message == "Error: unable to parse project meta data. Required attribute 'repositories[1].id' is undefined."
    }
}
