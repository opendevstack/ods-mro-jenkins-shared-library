import org.ods.util.PipelineSteps
import org.ods.util.Project

def call(Map config) {
    def steps = new PipelineSteps(this)
    Project project
    def repos = []

    def debug = config.get('debug', false)
    def odsImageTag = config.odsImageTag
    if (!odsImageTag) {
        error "You must set 'odsImageTag' in the config map"
    }
    def versionedDevEnvsEnabled = config.get('versionedDevEnvs', false)

    node {

        def scmBranches = scm.branches
        def branch = scmBranches[0]?.getName()
        if (branch && !branch.startsWith("*/")) {
            scmBranches = [[name: "*/${branch}"]]
        }

        // checkout local branch
        checkout([
            $class: 'GitSCM',
            branches: scmBranches,
            doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
            extensions: [[$class: 'LocalBranch', localBranch: "**"]],
            userRemoteConfigs: scm.userRemoteConfigs
        ])

        def steps = new PipelineSteps(this)
        def envs = Project.getBuildEnvironment(steps, debug, versionedDevEnvsEnabled)

        withPodTemplate(odsImageTag) {

            withEnv (envs) {

                def ciSkip = false

                stage('Init') {
                    steps.echo("**** STARTING stage Init ****")
                    def result = phaseInit()
                    if (result) {
                        project = result.project
                        repos = result.repos
                    } else {
                        ciSkip = true
                    }
                    steps.echo("**** ENDED stage Init ****")
                }

                if (ciSkip) {
                    return
                }

                stage('Build') {
                    steps.echo("**** STARTING stage Build ****")
                    phaseBuild(project, repos)
                    steps.echo("**** ENDED stage Build ****")
                }

                stage('Deploy') {
                    steps.echo("**** STARTING stage Deploy ****")
                    phaseDeploy(project, repos)
                    steps.echo("**** ENDED stage Deploy ****")
                }

                stage('Test') {
                    steps.echo("**** STARTING stage Test ****")
                    phaseTest(project, repos)
                    steps.echo("**** ENDED stage Test ****")
                }

                stage('Release') {
                    steps.echo("**** STARTING stage Release ****")
                    phaseRelease(project, repos)
                    steps.echo("**** ENDED stage Release ****")
                }

                stage('Finalize') {
                    steps.echo("**** STARTING stage Finalize ****")
                    phaseFinalize(project, repos)
                    steps.echo("**** ENDED stage Finalize ****")
                }
            }
        }
    }
}

private def withPodTemplate(String odsImageTag, Closure block) {
    def podLabel = "mro-jenkins-agent-${env.BUILD_NUMBER}"
    podTemplate(
        label: podLabel,
        cloud: 'openshift',
        containers: [
            containerTemplate(
                name: 'jnlp',
                image: "${env.DOCKER_REGISTRY}/cd/jenkins-slave-base:${odsImageTag}",
                workingDir: '/tmp',
                resourceRequestMemory: '512Mi',
                resourceLimitMemory: '1Gi',
                resourceRequestCpu: '200m',
                resourceLimitCpu: '1',
                alwaysPullImage: true,
                args: '${computer.jnlpmac} ${computer.name}',
                envVars: []
            )
        ],
        volumes: [],
        serviceAccount: 'jenkins',
    ) {
        block()
    }
}

return this
