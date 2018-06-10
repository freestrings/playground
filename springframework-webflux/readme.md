# RestTemplate on Spring Boot 1.x vs WebClient on Spring Boot 2.x

## 테스트 환경

1. Gatling -> Spring Boot, RestTemplate: 8082 -> nginx:8081
2. Gatling -> Spring Boot, WebClient: 8083 -> nginx:8081


## 실행

```bash
cd /home/han/Downloads

echo ok > test.txt

docker run --name nginx --rm -v /home/han/Downloads:/usr/share/nginx/html:ro -p 8081:80 nginx:latest

cd springframework-webflux

#
# DSIM_USERS 수치는 조정해보며 테스트 하면 된다.
#

# results/resttemplate_result.html 참고
./gradlew -p load-scripts -DTARGET_URL=http://localhost:8082 -DSIM_USERS=10 gatlingRun

# results/webclient_result.html 참고
./gradlew -p load-scripts -DTARGET_URL=http://localhost:8083 -DSIM_USERS=10 gatlingRun
```
