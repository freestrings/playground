#!/usr/bin/env bash

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic TextLinesTopic \
    --partitions 1 --replication-factor 1

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic UppercasedTextLinesTopic \
    --partitions 1 --replication-factor 1

docker exec broker \
    kafka-topics --bootstrap-server broker:9092 \
    --create --topic OriginalAndUppercasedTopic \
    --partitions 1 --replication-factor 1
