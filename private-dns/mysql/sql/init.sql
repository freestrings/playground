create database db;
create user 'user1'@'%' identified by '1111';
grant all on db.* to 'user1'@'%';
flush privileges;