#!/bin/bash

kill -SIGQUIT $(cat /var/run/nginx."$1".pid)