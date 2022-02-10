#!/bin/bash

master_status=`mysql -h mha-master -uroot -p1234 -e "SHOW MASTER STATUS"`
current_log=`echo $master_status | awk '{print $6}'`
current_pos=`echo $master_status | awk '{print $7}'`

stmt="CHANGE MASTER TO \
    GET_MASTER_PUBLIC_KEY=1, \
    MASTER_HOST='mha-master', \
    MASTER_USER='rep', \
    MASTER_PASSWORD='1234', \
    MASTER_LOG_FILE='$current_log', \
    MASTER_LOG_POS=$current_pos;"

mysql -uroot -p1234 -e "$stmt"

mysql -uroot -p1234 -e "start slave;"