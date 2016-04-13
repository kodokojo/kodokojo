#!/bin/bash

docker pull mesosphere/mesos-slave:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/mesos-master:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/marathon
docker pull jplock/zookeeper
docker pull haproxy
docker pull jenkins
docker pull gitlab/gitlab-ce:8.5.8-ce.0
docker pull nginx:1.9
docker pull node:5.7
docker pull java:8-jre
docker pull redis

git clone git@github.com:kodokojo/commons-tests.git
git clone git@github.com:kodokojo/commons.git
git clone git@github.com:kodokojo/kodokojo-ui.git
git clone git@github.com:kodokojo/kodokojo-haproxy-marathon.git

pushd commons-tests
mvn clean install
popd

pushd commons
mvn clean install verify
popd

pushd kodokojo-haproxy-marathon
chmod +x build.sh
./build.sh
popd

pushd kodokojo-ui
chmod +x build.sh
./build.sh
popd

mvn -P docker clean install verify
