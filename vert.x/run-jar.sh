#!/bin/bash

./gradlew clean shadowJar && java -DactiveProfile=dev \
    -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
    -jar build/libs/vert.x-1.0-SNAPSHOT-fat.jar