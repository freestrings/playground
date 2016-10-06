# 'test' 프로파일
Gradle로 실행
```
gradle test
```

또는 IntelliJ에서 ./src/test/java/fs/AllTests.java 실행

# 'development' 프로파일

### Outbound 서버실행
```
cd ./src/test/resources/data
python -m SimpleHTTPServer
```

## VM 옵션
IntelliJ에서 fs.IntegrationApp.java 실행옵션으로 지정 

```
-Dspring.profiles.active=development
```

# 'production' 프로파일

## TODO
- MySQL datasource 설정

## Build
```
gradle clean build
```

## 커멘드라인 실행
```
sudo java -Dspring.profiles.active=production -jar build/libs/fs-integration-1.0-SNAPSHOT.jar
```

또는 IntelliJ에서 -Dspring.profiles.active=production 옵션으로 실행
