
def build(projectName) {
    ws("${projectName}/workspace") {
        git credentialsId: 'github', url: "https://github.com/kodokojo/${projectName}.git"
        sh '''containerId=$(docker create -w /usr/src/workspace/ -e "DOCKER_HOST=unix:///var/run/docker.sock" -e "DOCKER_HOST_IP=172.31.43.37" -v /var/run/docker.sock:/var/run/docker.sock:rw -v /tmp/kodokojo/.m2:/root/.m2:rw maven:3-jdk-8 mvn install)
docker cp $(pwd)/ $containerId:/usr/src/
docker start -a $containerId
docker cp $containerId:/usr/src/workspace/target/ $PWD/target
docker rm $containerId'''
    }

}

def branches = [:]
branches['front'] = {
    node() {
        ws("kodokojo-ui/workspace") {
            git credentialsId: 'github', url: "https://github.com/kodokojo/kodokojo-ui.git"
            sh '''docker build -t="kodokojo/kodokojo-ui:builder" docker/builder/'''
            sh '''containerId=$(docker create  -e "KODOKOJO_UI_VERSION=0.1.0" kodokojo/kodokojo-ui:builder)
docker cp $PWD/. $containerId:/src/
docker start -a $containerId
docker cp $containerId:/target/. $PWD/docker/delivery/
docker rm $containerId
cd $PWD/docker/delivery
mkdir -p static || true
tar zxvf kodokojo-ui-0.1.0.tar.gz -C static
cd ../..
docker build --no-cache -t="kodokojo/kodokojo-ui" docker/delivery
'''

        }
    }
}

branches['back'] = {
    node() {
        build('commons-tests')
        build('commons')
        ws("kodokojo/workspace") {
            git credentialsId: 'github', url: "https://github.com/kodokojo/kodokojo.git"
            sh '''containerId=$(docker create -w /usr/src/workspace/ -e "DOCKER_HOST=unix:///var/run/docker.sock" -v /var/run/docker.sock:/var/run/docker.sock:rw -v /tmp/kodokojo/.m2:/root/.m2:rw maven:3-jdk-8 mvn -Dmaven.test.skip=true install)
docker cp $(pwd)/ $containerId:/usr/src/
docker start -a $containerId
docker cp $containerId:/usr/src/workspace/target/. $PWD/target
docker rm $containerId
cp $PWD/target/kodokojo-*-SNAPSHOT-runnable.jar $PWD/src/main/docker/local/kodokojo.jar
docker build -t="kodokojo/kodokojo" $PWD/src/main/docker/local/
'''
        }
    }
}

stage concurrency: 2, name: 'build docker images'
parallel branches

stage 'push docker images'
node() {

    input 'Push docker image to Docker Hub ?'
    sh '''docker push kodokojo/kodokojo-ui'''
    sh '''docker push kodokojo/kodokojo'''
}

