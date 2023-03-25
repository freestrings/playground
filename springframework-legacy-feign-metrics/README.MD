스프링 부트 1.5에서 http_server_requests_seconds.* 메트릭 수집 테스트

이슈

1. feign vs openfeign 
- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-feign
- 1.4.7.RELEASE 까지 릴리즈 됨
- 1.4.0 부터는 spring clound starter openfeign 를 내부적으로 디펜던시 걸고 있음
- 1.3.x 까지는 io.github.openfeign.* 을 쓰고 있음

- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-openfeign
  spring clound starter openfeign
- 1.4.0.RELEASE 부터

2. 스프링 부트 micrometer 지원 버전
https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Release-Notes#micrometer
micromiter는 spring boot 2.0 부터 지원
=> spring boot 1.5.x는 micromiter 메트릭 적용은 안됨

해결

1. openfeign 10.9 부터 feign-micrometer 지원
https://github.com/OpenFeign/feign/releases/tag/10.9
https://mvnrepository.com/artifact/io.github.openfeign/feign-micrometer/10.9

2. openfeign-starter 기준 2.2.3 부터 feign-core 10.10.1 임
https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-openfeign/2.2.3.RELEASE
=> feign-micrometer 쓰려면 openfeign-starter 2.2.3 이상 필요

.. 

다 필요 없고 추가 하면 됨 
```
compile 'io.micrometer:micrometer-spring-legacy:1.0.3'
compile 'io.micrometer:micrometer-registry-prometheus:1.0.3'
```