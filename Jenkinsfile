def odsImageTag = env.ODS_IMAGE_TAG ?: 'latest'
def odsGitRef = env.ODS_GIT_REF ?: 'production'
def final projectId = 'prov'
def final componentId = 'jenkins-mro'
def final credentialsId = "${projectId}-cd-cd-user-with-password"
def dockerRegistry
node {
  dockerRegistry = env.DOCKER_REGISTRY
}

@Library('ods-jenkins-shared-library@${odsGitRef}') _

// See readme of shared library for usage and customization.
odsPipeline(
  image: "${dockerRegistry}/cd/jenkins-slave-maven:${odsImageTag}",
  projectId: projectId,
  componentId: componentId,
  branchToEnvironmentMapping: [
    '*': 'dev'
  ],
  sonarQubeBranch: '*'
) { context ->
  stageBuild(context)
  stageScanForSonarqube(context)
}

def stageBuild(def context) {
  def javaOpts = "-Xmx512m"
  def gradleTestOpts = "-Xmx128m"

  stage('Unit Test') {
    sh 'echo ${APP_DNS}'
    withEnv(["TAGVERSION=${context.tagversion}", "NEXUS_USERNAME=${context.nexusUsername}", "NEXUS_PASSWORD=${context.nexusPassword}", "NEXUS_HOST=${context.nexusHost}", "JAVA_OPTS=${javaOpts}","GRADLE_TEST_OPTS=${gradleTestOpts}","ENVIRONMENT=${springBootEnv}"]) {
      def status = sh(script: "./gradlew clean test jacocoTestReport --no-daemon --stacktrace", returnStatus: true)
      if (status != 0) {
        error "Build failed!"
      }
    }
  }
}
