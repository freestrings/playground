#!/bin/bash

param="-b -c200 -t 10s"

if [ "$1" = "nginx" ]
then
    docker run --rm -t --link nginx yokogawa/siege $param http://nginx/vertx
elif [ "$1" = "vertx" ]
then
    docker run --rm -t --link fs-vertx1 yokogawa/siege $param http://fs-vertx1:8080/vertx
else
    echo "./benchmark.sh [nginx|vertx]"
fi