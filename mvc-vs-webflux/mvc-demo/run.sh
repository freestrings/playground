#!/usr/bin/env bash

docker run --rm -it \
  --name=mvc \
  --link=delay \
  --cpuset-cpus=0 \
  -p 9001:9010 \
  fs/mvc