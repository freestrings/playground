FROM mysql:8.0.28

ENV MYSQL_ROOT_PASSWORD=1234 TZ=Asia/Seoul

COPY ssh /root/.ssh
COPY master/conf /etc/mysql/conf.d
COPY master/db-shl /docker-entrypoint-initdb.d/
COPY master/shl/init-master.sh /init-master.sh

RUN apt-get update && \
    apt-get install -y mha4mysql-node openssh-server