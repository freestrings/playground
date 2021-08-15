#!/usr/bin/env bash

docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:9092 \
  --topic TextLinesTopic