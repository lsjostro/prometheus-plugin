library('sdpj-core-library')
library('sdpj-build-agents-library')
library('sdpj-gitflow-library')
library('sdpj-git-library')
library('sdpj-docker-library')
library('sdpj-build-management-library')

def printEnv() {
  env.getEnvironment().each { name, value -> println "Name: $name -> Value $value" }
}
printEnv()

enableGitFlow(artifactType: 'maven')

pipeline {
    agent {
        kubernetes sdp.kubernetesAgent(containers: [
            sdp.gitFlowContainer(),
            sdp.mavenContainer()
        ])
    }

    environment {
        APPLICATION_NAME = getRepoName()
        MVN_CREDS = credentials('MVN_CREDS')
    }

    stages {
      stage('env') {
          steps {
            script {
              printEnv()
              sh 'env'
            }
         }
      }
        stage('pre-build') {
            steps {
                gitFlowStart()
            }
        }
        stage('build') {
            when {
                expression { gitFlowConfig().isBuildRedundant == false }
            }
            stages {
                stage('compile') {
                    steps {
                        container(sdp.mavenContainer().name) {
                            sh  """
                                mvn clean
                                mvn install
                                mvn hpi:hpi
                                """
                        }
                    }
                }
                stage('publish maven artifact') {
                    steps {
                        deployMavenArtifacts()
                    }
                }
            }
        }
        stage('post-build') {
            steps {
                gitFlowFinish()
            }
        }
    }
}