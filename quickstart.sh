#!/bin/bash
#
# Kodo Kojo - Software factory done right
# Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#


function build {
  DIRECTORY=$1
  pushd dep/$DIRECTORY
  echo "-- ${DIRECTORY} -----------------------------------------------------------------"
  chmod +x build.sh
  ./build.sh
  rc=$?
  if [[ $rc != 0 ]]; then
    exit $rc
  fi;
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
    pushd dep
    git clone https://github.com/kodokojo/${PROJECT}.git ${PROJECT}
    popd
  fi
}

DOCKER_BIN_PATH=$(type -a docker | head -n1 | awk '{print $3}')
if [ ! -x "$DOCKER_BIN_PATH" ]; then
  echo "Unable to find a docker executable, please install Docker"
  exit 1
fi


docker pull mesosphere/mesos-slave:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/mesos-master:0.28.0-2.0.16.ubuntu1404
docker pull mesosphere/marathon
docker pull jplock/zookeeper
docker pull gliderlabs/consul:latest
docker pull haproxy
docker pull jenkins:1.651-2-alpine
docker pull gitlab/gitlab-ce:8.5.8-ce.0
docker pull nginx:1.9
docker pull node:5.9.0
docker pull java:8-jre
docker pull maven:3-jdk-8
docker pull redis

#createOrUpdateGit commons-tests
#createOrUpdateGit commons
createOrUpdateGit kodokojo-ui
#createOrUpdateGit kodokojo-haproxy-marathon

build kodokojo-ui
rc=$?
if [[ $rc != 0 ]]; then
  exit $rc
fi

#build kodokojo-haproxy-marathon
#rc=$?
#if [[ $rc != 0 ]]; then
#  exit $rc
#fi


chmod +x build.sh
./build.sh
rc=$?
if [[ $rc != 0 ]]; then
  exit $rc
fi
