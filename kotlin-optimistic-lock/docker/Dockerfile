FROM openjdk:8
EXPOSE 8080
RUN mkdir -p /app/
ADD ./kotlin-optimistic-lock-0.0.1-SNAPSHOT.jar /app/app-0.0.1.jar
ENTRYPOINT ["java","-jar","/app/app-0.0.1.jar"]