pipeline {
    agent { label 'docker' }
    stages {
        stage('Build') {
            steps {
                script {
                    props=readProperties file: 'gradle.properties'
                    VERSION="${props.version}-${props.apiVersion}"
                }
                sh "docker build --tag ${GIT_COMMIT} --build-arg apiVersion=${props.apiVersion} ."
            }
        }
        stage('Publish Latest') {
            when { branch 'master' }
            steps {
                sh "docker tag ${GIT_COMMIT} fintlabs.azurecr.io/fint-xmi-ea-adapter:build.${BUILD_NUMBER}"
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker push fintlabs.azurecr.io/fint-xmi-ea-adapter:build.${BUILD_NUMBER}"
                }
            }
        }
        stage('Publish PR') {
            when { changeRequest() }
            steps {
                sh "docker tag ${GIT_COMMIT} fintlabs.azurecr.io/fint-xmi-ea-adapter:${BRANCH_NAME}.${BUILD_NUMBER}"
                withDockerRegistry([credentialsId: 'fintlabs.azurecr.io', url: 'https://fintlabs.azurecr.io']) {
                    sh "docker push fintlabs.azurecr.io/fint-xmi-ea-adapter:${BRANCH_NAME}.${BUILD_NUMBER}"
                }
            }
        }
    }
}
