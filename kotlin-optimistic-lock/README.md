=== CPU 제한이 docker-compose로 안돼서..

```
docker run --rm --name mysql \
    -p 3307:3306 \
    -e MYSQL_ROOT_PASSWORD=root \
    -v ${PWD}/docker/init.sql:/docker-entrypoint-initdb.d/init.sql \
    --cpuset-cpus=0 \
    mysql:5.7.22

docker run -it --rm --name web1 \
    -p 8081:8080 \
    --cpuset-cpus=1 \
    --link mysql \
    fs.playground/kotlin-optimistic-lock:0.0.1-SNAPSHOT

docker run -it --rm --name web2 \
    -p 8082:8080 \
    --cpuset-cpus=2 \
    --link mysql \
    fs.playground/kotlin-optimistic-lock:0.0.1-SNAPSHOT

docker run -it --rm --name nginx \
    -p 8000:80 \
    --cpuset-cpus=3 \
    -v ${PWD}/docker/nginx.conf:/etc/nginx/nginx.conf:ro \
    --link web1 \
    --link web2 \
    nginx
 ```