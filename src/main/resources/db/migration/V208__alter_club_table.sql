ALTER TABLE club ADD COLUMN normalized_name VARCHAR(50) COMMENT '검색용 정규화 이름';
UPDATE club SET normalized_name = LOWER(REPLACE(name, ' ', ''));
ALTER TABLE club MODIFY COLUMN normalized_name VARCHAR(50) NOT NULL COMMENT '검색용 정규화 이름';
DROP INDEX idx_club_name ON club;
CREATE INDEX idx_club_normalized_name ON club(normalized_name);
