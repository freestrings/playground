#!/bin/bash

set -e

function mysql_ping {
    mysql -h mysql -uroot -proot <<< "select 1" &>/dev/null
    echo "$?"
}

function wait_for_mysql {
    while status=$(mysql_ping); [ $status != "0" ]; do
        echo "wait mysql"
        sleep 1
    done
}

wait_for_mysql

echo "mysql is up"

python -m SimpleHTTPServer