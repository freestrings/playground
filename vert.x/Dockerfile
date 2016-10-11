FROM java:8
RUN apt-get update && apt-get install -y htop
ADD build/libs/vert.x-1.0-SNAPSHOT-fat.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory", "-DactiveProfile=dev", "-jar", "/app.jar"]