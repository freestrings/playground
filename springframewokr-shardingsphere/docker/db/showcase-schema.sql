DROP DATABASE if exists testa;

CREATE database testa;

use testa;

DROP TABLE if EXISTS person;
DROP TABLE if EXISTS animal;

CREATE TABLE `person`
(
    `person_seq`  int(10) unsigned NOT NULL COMMENT '일련번호',
    `person_type` varchar(2)   NOT NULL COMMENT '타입',
    `person_name` varchar(100) NOT NULL DEFAULT '' COMMENT '이름',
    PRIMARY KEY (`person_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='사람';

CREATE TABLE `animal`
(
    `animal_seq`  int(10) unsigned NOT NULL COMMENT '일련번호',
    `animal_type` varchar(2)   NOT NULL COMMENT '타입',
    `animal_name` varchar(100) NOT NULL DEFAULT '' COMMENT '이름',
    PRIMARY KEY (`animal_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='동물';


CREATE TABLE `type_info`
(
    `type` varchar(2)   NOT NULL COMMENT '타입',
    `desc` varchar(100) NOT NULL COMMENT '설명',
    PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='타입정보';
