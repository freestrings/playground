#!/usr/bin/env bash

docker run --rm -it \
    --name "app_opti2" \
    --link mysql \
    -p "8082":8080 \
    --cpuset-cpus=6,7 \
    -e "SPRING_PROFILES_ACTIVE=optimistic,docker" \
    fs.playground/eventsourcing:0.0.1-SNAPSHOT