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


DOCKER_BIN_PATH=$(type -a docker | head -n1 | awk '{print $3}')
if [ ! -x "$DOCKER_BIN_PATH" ]; then
  echo "Unable to find a docker executable, please install Docker"
  exit 1
fi

DOCKER_CERT_OPT=""
if [ -x "$DOCKER_CERT_PATH" ]; then
  DOCKER_CERT_OPT="-v $DOCKER_CERT_PATH:$DOCKER_CERT_PATH"
fi

docker run -it --rm $DOCKER_CERT_OPT -v /tmp/kodokojo/.m2:/root/.m2 -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven -e "DOCKER_HOST=$DOCKER_HOST" -e "DOCKER_CERT_PATH=$DOCKER_CERT_PATH" -v ${DOCKER_BIN_PATH}:/usr/bin/docker maven:3-jdk-8 mvn clean install verify
rc=$?
if [[ $rc != 0 ]]; then
  exit $rc
fi

mkdir -p target/docker | true
cp src/main/docker/local/Dockerfile target/docker/
artifact=$(ls target | egrep kodokojo-.*-runnable.jar)
cp target/$artifact target/docker/kodokojo.jar
docker build -t="kodokojo/kodokojo" target/docker/
