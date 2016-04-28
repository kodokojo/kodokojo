#!/bin/bash

DOCKER_BIN_PATH=$(type -a docker | awk '{print $3}')
if [ ! -x "$DOCKER_BIN_PATH" ]; then
  echo "Unable to find a docker executable, please install Docker"
  exit 1
fi

docker run -it --rm -v $DOCKER_CERT_PATH:$DOCKER_CERT_PATH -v /tmp/kodokojo/.m2:/root/.m2 -v "$PWD":/usr/src/mymaven -w /usr/src/mymaven -e "DOCKER_HOST=$DOCKER_HOST" -e "DOCKER_CERT_PATH=$DOCKER_CERT_PATH" -v ${DOCKER_BIN_PATH}:/usr/bin/docker maven:3-jdk-8 mvn -P docker install verify
rc=$?
if [[ $rc != 0 ]]; then
  exit $rc
fi