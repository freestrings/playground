FROM java:8
VOLUME /tmp
ADD springframework-zuulproxy-1.0-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
