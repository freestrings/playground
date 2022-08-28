#FROM azul/zulu-openjdk-alpine:17
FROM azul/zulu-openjdk-alpine:11
#FROM azul/zulu-openjdk-alpine:8

ENV OTEL_AGENT_VERSION=0.17.0 \
    OTEL_AGENT_JAR_FILE=opentelemetry-javaagent-all-0.17.0.jar \
    APP_HOME=/app \
    MIN_HEAP_SIZE="40M" MAX_HEAP_SIZE="512M" \
    JAVA_GC_ARGS="-XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:+UseSerialGC -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90" \
    JAVA_OPTS="-server -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/./urandom -XX:CompressedClassSpaceSize=64m -XX:MaxMetaspaceSize=256m"

RUN mkdir -p ${APP_HOME}
COPY ./agent/${OTEL_JAR_FILE} ${APP_HOME}/
COPY ./scripts/start-app.sh ${APP_HOME}/

CMD ["sh", "/app/start-app.sh"]
