#!/usr/bin/env bash

docker run --rm -it \
  --link=webflux \
  --cpuset-cpus=2 \
  -p 8082:8080 \
  fs/bench $1