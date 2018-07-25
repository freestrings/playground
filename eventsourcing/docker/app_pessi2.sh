#!/usr/bin/env bash

docker run --rm -it \
    --name "app_pessi2" \
    --link mysql \
    -p "8084":8080 \
    --cpuset-cpus=6,7 \
    -e "SPRING_PROFILES_ACTIVE=pessimistic,docker" \
    fs.playground/eventsourcing:0.0.1-SNAPSHOT