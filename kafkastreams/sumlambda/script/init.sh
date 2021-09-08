#!/usr/bin/env bash

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic numbers-topic \
    --partitions 1 --replication-factor 1

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic sum-of-odd-numbers-topic \
    --partitions 1 --replication-factor 1
