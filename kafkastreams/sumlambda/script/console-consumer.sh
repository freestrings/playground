#!/usr/bin/env bash

docker exec broker kafka-console-consumer \
  --topic sum-of-odd-numbers-topic --from-beginning \
  --bootstrap-server broker:9092 \
  --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
