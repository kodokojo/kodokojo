#!/bin/bash

function build {
  DIRECTORY=$1
  pushd dep/$DIRECTORY
  chmod +x build.sh
  ./build.sh
  popd
}

function createOrUpdateGit {
  PROJECT=$1
  if [ ! -d "dep" ]; then
    mkdir dep
  fi
  if [ -d "dep/$PROJECT" ]; then
    pushd dep/$PROJECT
    git pull --rebase
    popd
  else
    pushd dep/$PROJECT
    git clone git@github.com:kodokojo/${PROJECT}.git ${PROJECT}
    popd
  fi
}

DOCKER_BIN_PATH=$(type -a docker | awk '{print $3}')
if [ ! -x "$DOCKER_BIN_PATH" ]; then
  echo "Unable to find a docker executable, please install Docker"
  exit 1
fi


docker pull mesosphere/mesos-slave:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/mesos-master:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/marathon
docker pull jplock/zookeeper
docker pull haproxy
docker pull jenkins:1.651-alpine
docker pull gitlab/gitlab-ce:8.5.8-ce.0
docker pull nginx:1.9
docker pull node:5.7
docker pull java:8-jre
docker pull maven:3-jdk-8
docker pull redis

createOrUpdateGit commons-tests
createOrUpdateGit commons
createOrUpdateGit kodokojo-ui
createOrUpdateGit kodokojo-haproxy-marathon
createOrUpdateGit kodokojo

build commons-test
build commons
build kodokojo-ui
build kodokojo-haproxy-marathon

chmod +x build.sh
./build.sh

