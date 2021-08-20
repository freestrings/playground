#!/usr/bin/env bash

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic play-events \
    --partitions 1 --replication-factor 1

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic play-events-per-session \
    --partitions 1 --replication-factor 1
