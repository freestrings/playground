#!/bin/bash

function check {
    docker run -it --rm --network="$(basename `pwd`)_mha" --dns=10.5.3.2 praqma/network-multitool dig db.local-kbin.io \
        | grep -e ^db.local-kbin.io \
        | awk '{ print $5 }'
}

# docker-compose exec mha-master bash -c "mysqladmin -uroot -p1234 <<< echo shutdown"
docker-compose kill mha-master

origin=$(check)

echo "before: $origin"
sleep 1

while [ "$(check)" == "$origin" ]; do
    printf "."
    sleep 2
done

echo "done"
