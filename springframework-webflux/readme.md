# RestTemplate on Spring Boot 1.x vs WebClient on Spring Boot 2.x

## 테스트 환경

1. Gatling -> Spring Boot, RestTemplate: 8082 -> nginx:8081
2. Gatling -> Spring Boot, WebClient: 8083 -> nginx:8081


## Ngin 실행

```bash
echo ok > test.txt
docker run --name nginx --rm -v "$PWD":/usr/share/nginx/html:ro -p 8081:80 nginx:latest
```

## resttemplate 테스트 

DSIM_USERS 수치는 조정해보며 테스트 하면 된다.

```bash
./gradlew -p resttemplate bootRun
./gradlew -p load-scripts -DTARGET_URL=http://localhost:8082 -DSIM_USERS=100 gatlingRun
```

## webflux 테스트

DSIM_USERS 수치는 조정해보며 테스트 하면 된다.

```bash
./gradlew -p webflux bootRun
./gradlew -p load-scripts -DTARGET_URL=http://localhost:8083 -DSIM_USERS=100 gatlingRun
```
