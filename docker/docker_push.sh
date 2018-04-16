#!/bin/sh
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
sh gradlew -PdockerVersion=$ICONATOR_DOCKER_VERSION -PdockerImageName=$ICONATOR_DOCKER_IMAGE_NAME docker dockerTag