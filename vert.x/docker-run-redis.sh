#!/bin/bash

docker run --rm --name redis --cpuset-cpus=3 -it -p 6379:6379 redis
