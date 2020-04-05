FROM adoptopenjdk/openjdk11
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} delay.jar
ENTRYPOINT ["java",\
"-Djava.security.egd=file:/dev/./urandom",\
"-jar",\
"/delay.jar"]
