#!/usr/bin/env bash

docker run --rm -it \
  --name=webflux \
  --link=delay \
  --cpuset-cpus=0 \
  -p 9000:9010 \
  fs/webflux