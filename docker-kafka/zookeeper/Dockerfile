FROM docker.io/java:8-jdk-alpine

MAINTAINER Mark Han <freestrings@gmail.com>

RUN apk add --update python bash \
      && rm -rf /var/cache/apk/*

ENV KAFKA_VERSION="0.10.0.0" SCALA_VERSION="2.10" 

# 카프카에 내장된 주키퍼를 쓴다.
ADD download.py /tmp/download.py
RUN python /tmp/download.py \
        && tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /var/opt \
        && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz

VOLUME ["/zookeeper"]

ADD zk_properties.py /tmp/zk_properties.py
ADD start_zookeeper.sh /bin/start_zookeeper.sh

CMD ["/bin/bash", "start_zookeeper.sh"]

EXPOSE 2181 2888 3888 9010