FROM openjdk:8
EXPOSE 8080
RUN mkdir -p /app/
ADD ./kotlin-optimistic-lock-0.0.1-SNAPSHOT.jar /app/app-0.0.1.jar
RUN apt update && apt install -y mysql-client && rm -rf /var/lib/apt
COPY ./wait-until-run.sh /app/wait-until-run.sh
CMD /app/wait-until-run.sh
