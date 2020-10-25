DROP DATABASE if exists testa;

CREATE database testa;

use testa;

DROP TABLE if EXISTS person;
CREATE TABLE `person` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '이름',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='사람';