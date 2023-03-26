FROM azul/zulu-openjdk-alpine:11

RUN mkdir -p /app
COPY ./agent/opentelemetry-javaagent.jar /app/
COPY ./scripts/start-app.sh /app/

COPY ./build/libs/api-*-SNAPSHOT.jar /app/app.jar

RUN ls -al /app

ENTRYPOINT [ "/app/start-app.sh" ]