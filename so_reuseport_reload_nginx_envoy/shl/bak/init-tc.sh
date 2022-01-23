#!/bin/bash

nginx

# Set up the queuing discipline
tc qdisc add dev lo root handle 1: prio bands 4
tc qdisc add dev lo parent 1:1 handle 10: pfifo limit 1000
tc qdisc add dev lo parent 1:2 handle 20: pfifo limit 1000
tc qdisc add dev lo parent 1:3 handle 30: pfifo limit 1000

# Create a plug qdisc with 1 meg of buffer
nl-qdisc-add --dev=lo --parent=1:4 --id=40: plug --limit 1048576
# Release the plug
nl-qdisc-add --dev=lo --parent=1:4 --id=40: --update plug --release-indefinite

# Set up the filter, any packet marked with "1" will be
# directed to the plug
tc filter add dev lo protocol ip parent 1:0 prio 1 handle 1 fw classid 1:4

#iptables -t mangle -I OUTPUT -p tcp -s 127.0.0.1 --syn -j MARK --set-mark 1
