#!/bin/bash
container_id=$(docker-compose exec mysql hostname | tr -d '\r')
docker-compose exec mysql tail -f /var/lib/mysql/${container_id}.log
