#!/usr/bin/env bash

docker exec broker kafka-console-consumer \
  --topic streams-wordcount-output --from-beginning \
  --bootstrap-server broker:9092 \
  --property print.key=true \
  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
