# Gradle로 실행

```
./gradlew run <verticle canonical class name>
```

예)
./gradlew run -> fs.App 기본 
./gradlew run fs.redis.Redis -> fs.redis.Redis 실행 


# Build

```
./gradlew clean shadowJar
```

# Jar 실행

```
java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
    -jar build/libs/vert.x-1.0-SNAPSHOT-fat.jar
```

# 특정 Vertical 실행

## Jar
```
java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
    -cp build/libs/vert.x-1.0-SNAPSHOT-fat.jar \
    io.vertx.core.Launcher run <verticle canonical class name>
```

ex)
java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
    -cp build/libs/vert.x-1.0-SNAPSHOT-fat.jar \
    io.vertx.core.Launcher run "fs.verticals.Redis"