#!/usr/bin/env bash
docker run --rm -i \
    --link app \
    --name jmeter_find \
    -v `pwd`/jmeter/jmeter_find.jmx:/config.jmx \
    -v `pwd`/../out:/results jmeter -n -t /config.jmx -l /results/find.jtl