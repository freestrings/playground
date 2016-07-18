
# Start Graphite Server

```shell
docker run -d \                                                                                    1 ↵
  --name graphite \
  -p 81:80 \
  -p 2003:2003 \
  -p 8125:8125/udp \
  hopsoft/graphite-statsd
```

# Graphite 접속
```
 http://localhost:81
```