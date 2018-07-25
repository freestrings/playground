#!/usr/bin/env bash

docker run -it --rm --name nginx \
    -p 8002:80 \
    --cpuset-cpus=8 \
    -v ${PWD}/nginx/nginx_pessi.conf:/etc/nginx/nginx.conf:ro \
    --link app1 \
    --link app2 \
    nginx
