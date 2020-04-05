#!/usr/bin/env bash

docker run --rm -it \
  --link=mvc \
  --cpuset-cpus=2 \
  fs/bench mvc:8080\/$1
