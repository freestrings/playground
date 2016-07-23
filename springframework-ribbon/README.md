# Build Docker Images

```
mvn clean package docker:build
```

# Start Ribbon

```
docker run -it --name ribbon1 --rm --link eureka-server -p 9992:8080 freestrings/ribbon
```

# Call Remote Server

```
curl -s http://localhost:9992/yourenv | python -m json.tool
```
