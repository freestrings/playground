#!/usr/bin/env bash

docker run -it --rm --name nginx_opti \
    -p 8001:80 \
    --cpuset-cpus=8 \
    -v ${PWD}/nginx/nginx_opti.conf:/etc/nginx/nginx.conf:ro \
    --link app_opti1 \
    --link app_opti2 \
    nginx
