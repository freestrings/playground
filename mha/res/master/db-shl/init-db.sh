#!/bin/bash

init_sql=(
    "CREATE USER 'mha'@'%' identified by '1234';"
    "GRANT ALL PRIVILEGES ON *.* TO 'mha'@'%';"
    "CREATE USER 'rep'@'%' identified by '1234';"
    "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* to 'rep'@'%';"
    "FLUSH PRIVILEGES;"
)

for sql in "${init_sql[@]}"; do
    echo "run ==> $sql"
    mysql -uroot -p1234 -e "$sql"
done