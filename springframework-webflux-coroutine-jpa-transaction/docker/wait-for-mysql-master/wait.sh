#!/bin/bash

set -e

function mysql_ping {
    mysql -h mysql-master -uroot -p1234 <<< "select 1" &>/dev/null
    echo "$?"
}

function wait_for_mysql {
    while status=$(mysql_ping); [ $status != "0" ]; do
        echo "wait mysql-master"
        sleep 5
    done
}

wait_for_mysql

echo "mysql-master is up"


stmt="CREATE USER 'repl'@'%' IDENTIFIED BY '1234';"
mysql -h mysql-master -uroot -p1234 -e "$stmt"

stmt="GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';"
mysql -h mysql-master -uroot -p1234 -e "$stmt"

mysql -h mysql-master -uroot -p1234 -e "FLUSH PRIVILEGES;"
