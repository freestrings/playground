FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG WAR_FILE
COPY ${WAR_FILE} app.war
ENTRYPOINT ["java","-jar","/app.war"]