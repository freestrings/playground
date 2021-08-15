#!/usr/bin/env bash

docker exec broker kafka-console-consumer \
  --topic OriginalAndUppercasedTopic --from-beginning \
  --bootstrap-server broker:9092 \
  --property print.key=true
