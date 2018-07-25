#!/usr/bin/env bash

docker run --rm -it \
    --name "app_opti1" \
    --link mysql \
    -p "8081":8080 \
    --cpuset-cpus=4,5 \
    -e "SPRING_PROFILES_ACTIVE=optimistic,docker" \
    fs.playground/eventsourcing:0.0.1-SNAPSHOT