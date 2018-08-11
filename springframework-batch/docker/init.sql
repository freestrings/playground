create database testa;
create user 'user'@'%' identified by '1111';
grant all on *.* to 'user'@'%';
flush privileges;