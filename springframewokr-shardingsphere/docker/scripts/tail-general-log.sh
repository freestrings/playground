#!/bin/bash
container_id=$(docker-compose exec $1 hostname | tr -d '\r')
docker-compose exec $1 tail -f /var/lib/mysql/${container_id}.log
