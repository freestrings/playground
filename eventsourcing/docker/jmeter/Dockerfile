FROM alpine

ARG JMETER_VERSION="4.0"
ENV JMETER_HOME /opt/apache-jmeter-${JMETER_VERSION}
ENV JMETER_BIN ${JMETER_HOME}/bin
ENV JMETER_DOWNLOAD_URL http://mirrors.ocf.berkeley.edu/apache/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz
ARG TZ="Asia/Seoul"
COPY ./apache-jmeter-${JMETER_VERSION}.tgz /tmp/apache-jmeter-${JMETER_VERSION}.tgz
RUN apk update \
	&& apk upgrade \
	&& apk add ca-certificates \
	&& update-ca-certificates \
	&& apk add --update openjdk8-jre tzdata curl unzip bash \
	&& rm -rf /var/cache/apk/* \
    && mkdir -p /opt  \
    && tar -xzf /tmp/apache-jmeter-${JMETER_VERSION}.tgz -C /opt  \
    && rm -rf /tmp/apache-jmeter-${JMETER_VERSION}.tgz
ENV PATH $PATH:$JMETER_BIN
COPY entrypoint.sh /
WORKDIR	${JMETER_HOME}
ENTRYPOINT ["/entrypoint.sh"]