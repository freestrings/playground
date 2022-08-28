#!/bin/bash

set -e

if [ ! -f "${APP_HOME}/app.jar" ]; then
   echo "'${APP_HOME}/app.jar' not found! Exiting..."
   exit 1
fi

JAVAAGENT_CMD="-javaagent:${APP_HOME}/${OTEL_AGENT_JAR_FILE}"

java $JAVA_OPTS \
  $JAVAAGENT_CMD \
  $JAVA_OPTS_APPEND \
  -Xms${MIN_HEAP_SIZE} \
  -Xmx${MAX_HEAP_SIZE} \
  $JAVA_GC_ARGS \
  -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
  -jar ${APP_HOME}/app.jar \
  $PROG_ARGS
