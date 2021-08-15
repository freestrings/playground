#!/usr/bin/env bash

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic streams-plaintext-input \
    --partitions 1 --replication-factor 1

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic streams-wordcount-output \
    --partitions 1 --replication-factor 1

