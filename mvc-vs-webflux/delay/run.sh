#!/usr/bin/env bash

docker run --rm -it \
  --name=delay \
  --cpuset-cpus=1 \
  fs/delay