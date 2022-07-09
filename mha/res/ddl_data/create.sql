DROP DATABASE if exists testa;

CREATE database testa;

use testa;

DROP TABLE if EXISTS test;

CREATE TABLE `test`
(
    `seq`  int(10) unsigned NOT NULL COMMENT '일련번호',
    `name` varchar(100) NOT NULL DEFAULT '' COMMENT '이름',
    PRIMARY KEY (`seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='TEST';
