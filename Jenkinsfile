node() {
    stage('Building kodokojo back JAR') {
        docker.image('maven:3.3.3-jdk-8').inside(' -v /var/run/docker.sock:/var/tmp/docker.sock:rw -e "DOCKER_HOST=unix:///var/tmp/docker.sock" ') {
            checkout scm
            def version = version()
            def commit = commitSha1()
            def commitMessage = commitMessage()
            slackSend channel: '#dev', color: '#6CBDEC', message: "*Starting * build job ${env.JOB_NAME} ${env.BUILD_NUMBER} from branch *${env.BRANCH_NAME}* (<${env.BUILD_URL}|Open>).\nCommit `${commit}` message :\n```${commitMessage}```"
            sh 'mvn -B install'
            if (currentBuild.result != 'FAILURE') {
                slackSend channel: '#dev', color: 'good', message: "Building job ${env.JOB_NAME} in version $version from branch *${env.BRANCH_NAME}* on commit `${commit}` \n Job ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) *SUCCESS*."
                step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
                step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
            } else {
                slackSend channel: '#dev', color: 'danger', message: "Building job ${env.JOB_NAME} in version $version from branch *${env.BRANCH_NAME}* on commit `${commit}` \n Job ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) *FAILED*."
            }
        }
    }
}

def version() {
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}

def commitSha1() {
    sh 'git rev-parse HEAD > commit'
    def commit = readFile('commit').trim()
    sh 'rm commit'
    commit.substring(0,7)
}

def commitMessage() {
    sh 'git log --format=%B -n 1 HEAD > commitMessage'
    def commitMessage = readFile('commitMessage')
    sh 'rm commitMessage'
    commitMessage
}
