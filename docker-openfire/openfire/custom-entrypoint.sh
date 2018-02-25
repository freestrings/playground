#!/bin/bash

set -e

function wait_for_mysql() {
    while status=$(curl -s -o /dev/null -w "%{http_code}" http://mysql-wait:8000); [ $status != "200" ]; do
        echo "prepre to run"
        sleep 1
    done

    echo "mysql is ready"
    sleep 1
}

function copy_resources() {
    cp /opt/openfire/_lib/log4j.xml /usr/local/openfire/lib/log4j.xml
    cp /opt/openfire/_conf/openfire.xml /usr/local/openfire/conf_org/openfire.xml
    cp /opt/openfire/_plugins/hazelcast.jar /usr/local/openfire/plugins_org/hazelcast.jar
    # cp /opt/openfire/_plugins/messageLogger.jar /usr/local/openfire/plugins_org/messageLogger.jar
}

wait_for_mysql
copy_resources

exec $1