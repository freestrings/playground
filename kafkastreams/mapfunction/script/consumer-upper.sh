#!/usr/bin/env bash

docker exec broker kafka-console-consumer \
  --topic UppercasedTextLinesTopic --from-beginning \
  --bootstrap-server broker:9092
