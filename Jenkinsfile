def buildState = 'SUCCESS'
node() {
    stage('Building kodokojo back JAR') {
        docker.image('maven:3.3.3-jdk-8').inside(" -v /tmp/kodokojo/.m2:/root/.m2 -v /var/run/docker.sock:/var/tmp/docker.sock:rw -e \"DOCKER_HOST=unix:///var/tmp/docker.sock\"  -e \"DOCKER_HOST_IP=${env.DOCKER_HOST_IP}\"") {
            checkout scm
            def version = version()
            def commit = commitSha1()
            def commitMessage = commitMessage()
            slackSend channel: '#dev', color: '#6CBDEC', message: "*Starting * build job ${env.JOB_NAME} ${env.BUILD_NUMBER} from branch *${env.BRANCH_NAME}* (<${env.BUILD_URL}|Open>).\nCommit `${commit}` message :\n```${commitMessage}```"
            try {
                def mvnStatus = sh returnStatus: true, script: 'mvn -B install'
                if (mvnStatus == 0) {
                    slackSend channel: '#dev', color: 'good', message: "Building job ${env.JOB_NAME} in version $version from branch *${env.BRANCH_NAME}* on commit `${commit}` \n Job ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) *SUCCESS*."
                    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
                    step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
                    //    step([$class: 'JgivenReportGenerator', excludeEmptyScenarios: true, jgivenResults: 'target/jgiven-reports/json/*.json', reportConfigs: [[$class: 'HtmlReportConfig', customCssFile: '', customJsFile: '', title: "${env.BRANCH_NAME} build ${env.BUILD_NUMBER}"]]])
                } else {
                    buildState = 'FAILURE'
                    currentBuild.result = 'FAILURE'
                    slackSend channel: '#dev', color: 'danger', message: "Building job ${env.JOB_NAME} in version $version from branch *${env.BRANCH_NAME}* on commit `${commit}` \n Job ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) *FAILED*."
                }
            } catch (Exception e) {
                slackSend channel: '#dev', color: 'danger', message: "Building job ${env.JOB_NAME} in version $version from branch *${env.BRANCH_NAME}* on commit `${commit}` \n Job ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>) *FAILED*.\n"
                buildState = 'FAILURE'
                currentBuild.result = 'FAILURE'
            }

        }
    }
    if (buildState == 'SUCCESS') {
        buildAndPushDocker()
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
    commit.substring(0, 7)
}

def commitMessage() {
    sh 'git log --format=%B -n 1 HEAD > commitMessage'
    def commitMessage = readFile('commitMessage')
    sh 'rm commitMessage'
    commitMessage
}

def buildAndPushDocker() {
    stage('Building docker image then Push it') {
        def version = version()
        def commit = commitSha1()
        def imageName = "kodokojo/kodokojo:dev"
        try {

            sh 'mkdir -p target/docker'
            sh 'cp src/main/docker/local/Dockerfile target/docker/'
            sh "cp target/kodokojo-${version}-runnable.jar target/docker/kodokojo.jar"
            sh "docker build -t=\"${imageName}\" target/docker/ && docker push ${imageName}"

            slackSend channel: '#dev', color: '#6CBDEC', message: "Build and push Docker image *${imageName}* from branch *${env.BRANCH_NAME}* on commit `${commit}` *SUCCESS*."
        } catch (Exception e) {
            slackSend channel: '#dev', color: 'danger', message: "Build and push Docker image *${imageName}* from branch *${env.BRANCH_NAME}* on commit `${commit}` *FAILED*:\n```${e.getMessage()}```"
        }
    }
}

