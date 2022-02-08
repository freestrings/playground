#!/bin/bash
apt-get update
apt-get install -y mha4mysql-node openssh-server

mkdir /var/run/sshd
echo 'root:1234' | chpasswd
sed -i 's/#\?PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

cp -r /root/ssh /root/.ssh
chown -R root:root /root/.ssh
chmod 400 /root/.ssh/id_rsa_master
chmod 600 /root/.ssh/config
ssh-keyscan -t rsa -H mha-master >> /root/.ssh/known_hosts

service ssh start

ssh root@mha-master <<'RUNSHL'
ssh-keyscan -t rsa -H mha-slave >> /root/.ssh/known_hosts
RUNSHL

/entrypoint.sh mysqld