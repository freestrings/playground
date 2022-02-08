FROM mysql:8.0.28

ENV MYSQL_ROOT_PASSWORD=1234 TZ=Asia/Seoul

COPY ssh /root/.ssh
COPY slave/conf /etc/mysql/conf.d
COPY slave/db-shl /docker-entrypoint-initdb.d/
COPY slave/shl/init-slave.sh /init-slave.sh

RUN apt-get update && \
    apt-get install -y mha4mysql-node openssh-server