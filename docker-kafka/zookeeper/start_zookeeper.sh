#!/bin/bash
KAFKA_HOME=/var/opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION}
export KAFKA_HOME

echo ${ZK_MYID:-1} > /zookeeper/myid
unset ZK_MYID

python /tmp/zk_properties.py
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties.active &
KAFKA_PID=$!

wait
