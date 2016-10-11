#!/bin/bash

param="-k -n 10000 -c 200"

if [ "$1" = "nginx" ]
then
    docker run --rm -t --link nginx jordi/ab ab $param http://nginx/vetx
elif [ "$1" = "vertx" ]
then
    docker run --rm -t --link fs-vertx1 jordi/ab ab $param http://fs-vertx1:8080/vertx
else
    echo "./benchmark-ab.sh [nginx|vertx]"
fi