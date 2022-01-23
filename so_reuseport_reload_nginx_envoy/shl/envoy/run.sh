#!/bin/bash

nginx -c /etc/nginx.backend/nginx.conf
envoy -c /etc/envoy/envoy.yaml