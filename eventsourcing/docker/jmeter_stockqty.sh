#!/usr/bin/env bash
docker run --rm -i \
    --link nginx \
    --name jmeter_stockqty \
    -v `pwd`/jmeter/jmeter_stockqty.jmx:/config.jmx \
    -v `pwd`/../out:/results jmeter -n -t /config.jmx -l /results/stockqty.jtl