#
# mysql 최신 버전은 general log 위치가 다른가??
# my.cnf > general_log_file 옵션에 지정된 로그 파일에 기록되지 않는다.
#
docker run -it --rm --name mysql \
	-p 3306:3306 \
	-v ${PWD}/my.cnf:/etc/mysql/conf.d/my.cnf \
	-v ${PWD}/init.sql:/docker-entrypoint-initdb.d/init.sql \
	-e MYSQL_ROOT_PASSWORD=root \
	mysql:5.7.22