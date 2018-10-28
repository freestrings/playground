#!/bin/bash

set -e

function wait_for_mysql() {
    while status=$(curl -s -o /dev/null -w "%{http_code}" http://wait-for-mysql:8000); [ $status != "200" ]; do
        echo "prepre to run"
        sleep 1
    done

    echo "mysql is ready"
    sleep 1
}

wait_for_mysql

exec $1