docker run -it --rm --name mysql \
	-p 3307:3306 \
	-v ${PWD}/my.cnf:/etc/mysql/conf.d/my.cnf \
	-v ${PWD}/init.sql:/docker-entrypoint-initdb.d/init.sql \
	-e MYSQL_ROOT_PASSWORD=root \
	mysql:5.7.22