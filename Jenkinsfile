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
        stage('compile') {
            steps {
                container(sdp.mavenContainer().name) {
                    sh  """
                        export MAVEN_OPTS="-Xmx1024M -XX:MaxPermSize=256M"
                        mvn compile -B
                        """
                }
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
                                export MAVEN_OPTS="-Xmx1024M -XX:MaxPermSize=256M"
                                mvn clean package install -Dmaven.test.skip
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