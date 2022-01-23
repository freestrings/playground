#!/bin/bash

## envoy
# yum install -y yum-utils
# rpm --import 'https://rpm.dl.getenvoy.io/public/gpg.CF716AF503183491.key'
# curl -sL 'https://rpm.dl.getenvoy.io/public/config.rpm.txt?distro=el&codename=7' > /tmp/tetrate-getenvoy-rpm-stable.repo
# yum-config-manager -y --add-repo '/tmp/tetrate-getenvoy-rpm-stable.repo'
# yum makecache -y --disablerepo='*' --enablerepo='tetrate-getenvoy-rpm-stable'
# yum install -y getenvoy-envoy

curl -1sLf 'https://rpm.dl.getenvoy.io/public/setup.rpm.sh' | bash
yum install -y getenvoy-envoy

## nginx
yum install -y nginx

## iptables
yum install -y iptables