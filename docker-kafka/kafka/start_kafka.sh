#!/bin/bash
KAFKA_HOME=/var/opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION}
export KAFKA_HOME

KAFKA_ADVERTISED_LISTENERS="plaintext://$(ifconfig eth0 | grep "inet addr:" | cut -d : -f 2 | cut -d " " -f 1):9092"
export KAFKA_ADVERTISED_LISTENERS

python /tmp/server_properties.py
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties.active &
KAFKA_PID=$!

wait
