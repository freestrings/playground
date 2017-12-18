#!/usr/bin/env bash

URL="https://dl.bintray.com/marcingrzejszczak/maven/stub-runner-boot-1.1.0.RELEASE.jar"
VERSION="1.1.0.RELEASE"
JAR_NAME="stub-runner-boot-${VERSION}"
JAR_LOCATION="target/${JAR_NAME}.jar"

mkdir -p target
curl -L "${URL}" -o "${JAR_LOCATION}"

echo "Running stub runner"
java -jar "${JAR_LOCATION}" \
    --stubrunner.workOffline="true" \
    --stubrunner.ids="wmp.tnc:demo-backend-api:+:8090" 2>&1
