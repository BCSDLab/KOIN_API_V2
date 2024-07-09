CREATE TABLE IF NOT EXISTS coop_shop (
    id			INT UNSIGNED	NOT NULL	COMMENT 'coop 고유 id'	PRIMARY KEY		AUTO_INCREMENT,
    name		VARCHAR(50)		NOT NULL	COMMENT '생협 매장 이름',
    phone		VARCHAR(50)					COMMENT '생협 매장 연락처',
    location	VARCHAR(50)		NOT NULL	COMMENT '생협 매장 위치',
    remarks 	TEXT						COMMENT '특이사항',
    is_deleted  TINYINT(1)      NOT NULL    COMMENT '삭제 여부'           DEFAULT 0,
    created_at 	TIMESTAMP		NOT NULL	COMMENT '생성 일자'			DEFAULT CURRENT_TIMESTAMP,
    updated_at 	TIMESTAMP		NOT NULL	COMMENT '업데이트 일자'		DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coop_opens (
    id 			INT UNSIGNED	NOT NULL	COMMENT 'coop_open 고유 id'	PRIMARY KEY		AUTO_INCREMENT,
    coop_id 	INT UNSIGNED	NOT NULL	COMMENT '생협 매장 id',
    type		VARCHAR(10)					COMMENT '기타 타입(아침, 점심, 저녁)',
    day_of_week VARCHAR(10)		NOT NULL	COMMENT '요일',
    open_time 	VARCHAR(10)					COMMENT '오픈 시간',
    close_time 	VARCHAR(10)					COMMENT '마감 시간',
    is_deleted  TINYINT(1)      NOT NULL    COMMENT '삭제 여부'           DEFAULT 0,
    created_at 	TIMESTAMP		NOT NULL	COMMENT '생성 일자'			DEFAULT CURRENT_TIMESTAMP,
    updated_at 	TIMESTAMP		NOT NULL	COMMENT '업데이트 일자'		DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_COOP_ID FOREIGN KEY (coop_id) REFERENCES coop_shop(id)		ON DELETE CASCADE
);
