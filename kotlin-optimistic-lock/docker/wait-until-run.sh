#!/bin/bash
until echo '\q' | mysql -hmysql -uroot -prootpassword; do
    >&2 echo "MySQL is unavailable - sleeping"
    sleep 1
done

java -jar /app/app-0.0.1.jar
