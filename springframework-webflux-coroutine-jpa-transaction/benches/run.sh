#!/usr/bin/env bash

# master, slave 분기가 잘 되는지 확인
siege -c10 -i -f ./urls.txt