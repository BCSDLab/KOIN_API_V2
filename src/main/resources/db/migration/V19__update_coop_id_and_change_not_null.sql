UPDATE coop
    JOIN users ON coop.user_id = users.id
    SET coop.coop_id = users.email;

ALTER TABLE `koin`.`coop`
    CHANGE COLUMN `coop_id` `coop_id` VARCHAR(255) NOT NULL COMMENT '영양사 id, 일반 로그인 형식' ;
