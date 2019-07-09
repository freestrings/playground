```
# run a
gradle build && mv build/libs/haproxy-ws-0.0.1-SNAPSHOT.jar build/libs/a.jar \
	&& docker run -it --rm --name server_a `pwd`/build/libs/a.jar:/app.jar openjdk:8-jdk-alpine java -jar /app.jar

# run b
gradle build && mv build/libs/haproxy-ws-0.0.1-SNAPSHOT.jar build/libs/b.jar \
	&& docker run -it --rm --name server_b `pwd`/build/libs/b.jar:/app.jar openjdk:8-jdk-alpine java -jar /app.jar

# run c
docker run -it --rm --name server_c ubuntu bash
apt update
apt install python
python -m SimpleHTTPServer


# run haproxy
docker run -it --rm --name haproxy \
	--link server_a \
	--link server_b \
	--link server_c \
	--link syslog \
	-v `pwd`/haproxy.cfg:/haproxy.cfg haproxy -f /haproxy.cfg


```
