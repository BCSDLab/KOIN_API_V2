-- new_articles 테이블에 old_id 추가
ALTER TABLE `koin`.`new_articles`
    ADD COLUMN `old_id` INT NULL DEFAULT '0';

-- articles 데이터를 new_articles로 이관하면서 old_id 저장
INSERT INTO `koin`.`new_articles` (board_id, title, content, hit, is_notice, is_deleted, created_at, updated_at, old_id)
SELECT board_id,
       title,
       content,
       hit,
       is_notice,
       is_deleted,
       created_at,
       updated_at,
       a.id
FROM `koin`.`articles` a
WHERE a.board_id NOT IN (5, 6, 7, 8);

-- new_koin_articles에 new_articles.old_id를 매핑하여 article_id를 삽입
INSERT INTO `koin`.`new_koin_articles` (article_id, user_id, is_deleted, created_at, updated_at)
SELECT na.id, a.user_id, a.is_deleted, a.created_at, a.updated_at
FROM `koin`.`articles` a
         JOIN `koin`.`new_articles` na ON a.id = na.old_id
WHERE a.board_id NOT IN (5, 6, 7, 8);

-- new_articles에서 old_id 컬럼 삭제
ALTER TABLE `koin`.`new_articles`
DROP COLUMN old_id;
