#!/bin/bash

set -e

function mysql_ping {
    mysql -h mysql-master -uroot -p1234 <<< "select 1" &>/dev/null
    echo "$?"
}

function wait_for_mysql {
    while status=$(mysql_ping); [ $status != "0" ]; do
        echo "wait mysql-master on slave-waiter"
        sleep 5
    done
}

wait_for_mysql
sleep 5

function mysql_ping_slave {
    mysql -h mysql-slave -utestuser -p1234 <<< "select 1" &>/dev/null
    echo "$?"
}

function wait_for_mysql_slave {
    while status=$(mysql_ping_slave); [ $status != "0" ]; do
        echo "wait mysql-slave"
        sleep 5
    done
}

wait_for_mysql_slave

echo "mysql-slave is up"

master_status=`mysql -h mysql-master -uroot -p1234 -e "SHOW MASTER STATUS"`
current_log=`echo $master_status | awk '{print $6}'`
current_pos=`echo $master_status | awk '{print $7}'`

echo "#### $current_log"
echo "#### $current_pos"

stmt="CHANGE MASTER TO \
    GET_MASTER_PUBLIC_KEY=1, \
    MASTER_HOST='mysql-master', \
    MASTER_USER='repl', \
    MASTER_PASSWORD='1234', \
    MASTER_LOG_FILE='$current_log', \
    MASTER_LOG_POS=$current_pos;"
mysql -h mysql-slave -uroot -p1234 -e "$stmt"

mysql -h mysql-slave -uroot -p1234 -e "SHOW SLAVE STATUS \G"

mysql -h mysql-slave -uroot -p1234 -e "START SLAVE USER='repl' PASSWORD='1234'; "

all_sqls=(
    "showcase-schema.sql"
    )

sqlExecute() {
	path=$1
	shift
	sqls=("${@}")

	for file in "${sqls[@]}"
	do
		echo "- import: /$path/$file"
		mysql -h mysql-master -uroot -p1234 < "/$path/$file"
	done
}

sqlExecute "db" "${all_sqls[@]}"
