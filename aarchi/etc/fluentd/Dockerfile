FROM fluent/fluentd:v1.12.0-debian-1.0
USER root

COPY ./fluent.conf /fluentd/etc/fluent.conf

# https://stackoverflow.com/questions/71120621/efk-system-is-build-on-docker-but-fluentd-cant-start-up
RUN gem uninstall -I elasticsearch && gem install elasticsearch -v 7.17.0

RUN ["gem", "install", "fluent-plugin-elasticsearch", "--no-document", "--version", "5.0.3"]
USER fluent