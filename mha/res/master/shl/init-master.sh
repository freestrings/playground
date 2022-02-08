#!/bin/bash
# apt-get update
# apt-get install -y mha4mysql-node openssh-server

mkdir /var/run/sshd
echo 'root:1234' | chpasswd
sed -i 's/#\?PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

chown -R root:root /root/.ssh
chmod 400 /root/.ssh/id_rsa_slave
chmod 600 /root/.ssh/config

service ssh start

/entrypoint.sh mysqld