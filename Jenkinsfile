pipeline {
    agent any

    stages {
        stage('Clean') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew assembleDebug'
            }
        }
        stage('Test') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew test --stacktrace'
            }
        }
    }

    triggers {
        pollSCM('* * * * *')
    }

    post {
        success {
            notify("success")
        }
        failure {
            notify("failure")
        }
    }
}

def notify(status) {
    def jobName = env.JOB_NAME + ' ' + env.GIT_BRANCH
    def buildNo = env.BUILD_NUMBER
    def gitCmd = "git log -1 --pretty=format:'Last Commit Number: %h\nAuthor: %an\nTime: %ar\nLast Commit Info: %s'"
    def description = sh(returnStdout: true, script: gitCmd).trim()

    sh "${jobName} Build #${buildNo} ${status}\n${description}"
}