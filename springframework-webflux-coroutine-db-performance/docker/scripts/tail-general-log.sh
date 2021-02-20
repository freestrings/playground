#!/bin/bash
container_id=$(docker-compose exec mysql-master hostname | tr -d '\r')
docker-compose exec $1 tail -f /var/lib/mysql/${container_id}.log
