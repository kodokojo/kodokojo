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

docker run -it --rm -v $DOCKER_CERT_PATH:$DOCKER_CERT_PATH -v /tmp/kodokojo/.m2:/root/.m2 -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven -e "DOCKER_HOST=$DOCKER_HOST" -e "DOCKER_CERT_PATH=$DOCKER_CERT_PATH" -v ${DOCKER_BIN_PATH}:/usr/bin/docker maven:3-jdk-8 mvn -P docker install verify
rc=$?
if [[ $rc != 0 ]]; then
  exit $rc
fi