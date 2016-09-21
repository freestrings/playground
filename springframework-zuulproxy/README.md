# Build Docker Images

```
mvn clean package docker:build
```

# Start ZuulProxy

```
docker run -it --name zuulserver --rm --link eureka-server -p 9993:8080 freestrings/zuulserver 
```

# Check Routes

```
http://localhost:9993/routes
{
    "/eureka-client/**": "eureka-client",
    "/ribbon-client/**": "ribbon-client"
}
```

# Call Service via Route

```
curl -s http://localhost:9993/eureka-client/myenv | python -m json.tool
{
    "hostaddress": "172.17.0.3",
    "hostname": "a8bf881cbd33"
}
curl -s http://localhost:9993/eureka-client/myenv | python -m json.tool
{
    "hostaddress": "172.17.0.4",
    "hostname": "a5279888f1bc"
}

# ignored patterns
curl -s http://localhost:9993/ribbon-client/yourenv | python -m json.tool
{
    "error": "Not Found",
    "message": "No message available",
    "path": "/ribbon-client/yourenv",
    "status": 404,
    "timestamp": 1469363058764
}
```