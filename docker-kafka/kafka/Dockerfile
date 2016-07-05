FROM docker.io/java:8-jdk-alpine

RUN apk add --update python bash \
      && rm -rf /var/cache/apk/*

ENV KAFKA_VERSION="0.10.0.0" SCALA_VERSION="2.10" 

ADD download.py /tmp/download.py
RUN python /tmp/download.py \
        && tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /var/opt \
        && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz

VOLUME ["/kafka"]

ADD server_properties.py /tmp/server_properties.py
ADD start_kafka.sh /bin/start_kafka.sh

CMD ["/bin/bash", "start_kafka.sh"]