def call(Map config) {

    def project = [:]
    def repos   = []

    def debug = config.get('debug', false)

    node {
        checkout scm

        def error
        try {
            withEnv (mroEnvironment(debug)) {
                stage('Init') {
                    echo "**** STARTING stage Init ****"
                    def result = phaseInit()
                    project = result.project
                    repos = result.repos
                    echo "**** ENDED stage Init ****"
                }

                stage('Build') {
                    echo "**** STARTING stage Build ****"
                    phaseBuild(project, repos)
                    echo "**** ENDED stage Build ****"
                }

                stage('Deploy') {
                    echo "**** STARTING stage Deploy ****"
                    phaseDeploy(project, repos)
                    echo "**** ENDED stage Deploy ****"
                }

                stage('Test') {
                    echo "**** STARTING stage Test ****"
                    phaseTest(project, repos)
                    echo "**** ENDED stage Test ****"
                }

                stage('Release') {
                    echo "**** STARTING stage Release ****"
                    phaseRelease(project, repos)
                    echo "**** ENDED stage Release ****"
                }

                stage('Finalize') {
                    echo "**** STARTING stage Finalize ****"
                    phaseFinalize(project, repos)
                    echo "**** ENDED stage Finalize ****"
                }
            }
        }
        catch (e) {
            error = e
            throw e
        }
        finally {
            project.reportPipelineStatus(error)
        }
    }
}

return this
