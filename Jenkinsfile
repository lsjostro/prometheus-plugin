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

enableGithubFlow(artifactType: 'maven')

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
        stage('pre-build') {
            steps {
                gitHubFlowStart()
            }
        }
        stage('compile') {
            steps {
                container(sdp.mavenContainer().name) {
                    sh "mvn compile -B"
                }
            }
        }
        stage('build') {
            stages {
                stage('compile') {
                    steps {
                        container(sdp.mavenContainer().name) {
                            sh  """
                                mvn clean install -Dmaven.test.skip
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
                gitHubFlowFinish()
            }
        }
    }
}