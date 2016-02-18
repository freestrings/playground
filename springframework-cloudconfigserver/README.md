# Redis
```bash
docker run -d --name redis redis
```

# Mysql

```bash
docker run --name master-mysql -e MYSQL_ROOT_PASSWORD=root -d -p 3308:3306 mysql:5.5
docker run --name salve-mysql -e MYSQL_ROOT_PASSWORD=root -d -p 3309:3306 mysql:5.5
docker run --name theother-mysql -e MYSQL_ROOT_PASSWORD=root -d -p 3310:3306 mysql:5.5
```

# Cloud Config Server

```bash
cd springframework-cloudconfigserver
#export CONFIG_PATH=$SOME_PATH
./gradlew booRun
```