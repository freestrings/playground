FROM adoptopenjdk/openjdk11
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} bench.jar
ENTRYPOINT ["java",\
"-jar",\
"/bench.jar"]
