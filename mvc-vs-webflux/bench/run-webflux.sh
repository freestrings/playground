#!/usr/bin/env bash

docker run --rm -it \
  --link=webflux \
  --cpuset-cpus=2 \
  fs/bench webflux:8080\/$1