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
        stage('Publish') {
            when { branch 'master' }
            steps {
                sh "docker tag ${GIT_COMMIT} dtr.fintlabs.no/beta/fint-xmi-ea-adapter:${VERSION}"
                withDockerRegistry([credentialsId: 'dtr-fintlabs-no', url: 'https://dtr.fintlabs.no']) {
                    sh "docker push 'dtr.fintlabs.no/beta/fint-xmi-ea-adapter:${VERSION}'"
                }
//                withDockerServer([credentialsId: "ucp-fintlabs-jenkins-bundle", uri: "tcp://ucp.fintlabs.no:443"]) {
//                    sh "docker service update fake-adapters_student --image dtr.fintlabs.no/beta/fint-xmi-ea-adapter:latest --detach=false"
//                }
            }
        }
        stage('Publish PR') {
            when { changeRequest() }
            steps {
                sh "docker tag ${GIT_COMMIT} dtr.fintlabs.no/beta/fint-xmi-ea-adapter:${BRANCH_NAME}"
                withDockerRegistry([credentialsId: 'dtr-fintlabs-no', url: 'https://dtr.fintlabs.no']) {
                    sh "docker push 'dtr.fintlabs.no/beta/fint-xmi-ea-adapter:${BRANCH_NAME}'"
                }
            }
        }
    }
}
