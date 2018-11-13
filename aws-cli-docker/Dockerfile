FROM alpine:latest

LABEL maintainer="Changseok Han<freestrings@gmail.com>"

RUN apk add tzdata && \
  cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
  echo "Asia/Seoul" > /etc/timezone && \
  apk del tzdata

RUN apk update

RUN apk add --no-cache --virtual .build-deps
RUN apk add bash

RUN apk -Uuv add groff less python py-pip
RUN pip install awscli
RUN apk --purge -v del py-pip
RUN rm /var/cache/apk/*

CMD ["/bin/bash"]