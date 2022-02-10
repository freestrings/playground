#!/bin/bash

mysql -uroot -p1234 <<END
    CREATE USER 'mha'@'%' identified by '1234';
    GRANT ALL PRIVILEGES ON *.* TO 'mha'@'%';
    CREATE USER 'rep'@'%' identified by '1234';
    GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* to 'rep'@'%';
    FLUSH PRIVILEGES;
END
