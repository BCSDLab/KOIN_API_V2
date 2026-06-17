alter table `lost_item_articles`
    add column `type`       VARCHAR(100)    NOT NULL comment '게시글 타입'           DEFAULT 'LOST',
    add column `is_council` TINYINT(1)      NOT NULL comment '작성자 총학생회 여부'   DEFAULT '0';
