#!/bin/bash

init_sql=(
    "CREATE USER 'mha'@'%' identified by '1234';"
    "GRANT ALL PRIVILEGES ON *.* TO 'mha'@'%';"
    "CREATE USER 'rep'@'%' identified by '1234';"
    "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* to 'rep'@'%'"
    "FLUSH PRIVILEGES;"
)

for sql in "${init_sql[@]}"; do
    echo "run ==> $sql"
    mysql -uroot -p1234 -e "$sql"
done

master_status=`mysql -h mha-master -uroot -p1234 -e "SHOW MASTER STATUS"`
current_log=`echo $master_status | awk '{print $6}'`
current_pos=`echo $master_status | awk '{print $7}'`

stmt="CHANGE MASTER TO \
    GET_MASTER_PUBLIC_KEY=1, \
    MASTER_HOST='mha-master', \
    MASTER_USER='mha', \
    MASTER_PASSWORD='1234', \
    MASTER_LOG_FILE='$current_log', \
    MASTER_LOG_POS=$current_pos;"

mysql -uroot -p1234 -e "$stmt"