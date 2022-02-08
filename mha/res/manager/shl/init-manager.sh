#!/bin/bash

set -x

mkdir /var/run/sshd
echo 'root:1234' | chpasswd
sed -i 's/#\?PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

cp -r /root/ssh /root/.ssh

chmod 400 /root/.ssh/id_rsa_master
chmod 400 /root/.ssh/id_rsa_slave
chmod 600 /root/.ssh/config
ssh-keyscan -t rsa -H mha-master >> /root/.ssh/known_hosts
ssh-keyscan -t rsa -H mha-slave >> /root/.ssh/known_hosts

service ssh start

nohup masterha_manager --conf=/etc/masterha_default.cnf --ignore_last_failover < /dev/null > /etc/masterha/log/mha.log 2>&1