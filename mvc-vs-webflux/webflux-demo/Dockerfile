FROM adoptopenjdk/openjdk11
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} webflux.jar
ENTRYPOINT ["java",\
"-Djava.security.egd=file:/dev/./urandom",\
"-Dcom.sun.management.jmxremote",\
"-Dcom.sun.management.jmxremote.port=9010",\
"-Dcom.sun.management.jmxremote.local.only=false",\
"-Dcom.sun.management.jmxremote.authenticate=false",\
"-Dcom.sun.management.jmxremote.ssl=false",\
"-Dreactor.netty.ioWorkerCount=1",\
"-jar",\
"/webflux.jar"]
