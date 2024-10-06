-- new_articles 테이블에 old_id 추가
ALTER TABLE `koin`.`new_articles`
    ADD COLUMN `old_id` INT NULL DEFAULT '0';

-- koreatech_articles 데이터를 new_articles로 이관하면서 old_id 저장
INSERT INTO `koin`.`new_articles` (board_id, title, content, hit, is_notice, is_deleted, created_at, updated_at, old_id)
SELECT ka.board_id,
       ka.title,
       ka.content,
       ka.hit,
       ka.is_notice,
       ka.is_deleted,
       ka.created_at,
       ka.updated_at,
       ka.id
FROM `koin`.`koreatech_articles` ka
ORDER BY ka.registered_at ASC, ka.id ASC;

-- koreatech_articles 데이터를 new_koreatech_articles로 이관하면서 old_id 저장
INSERT INTO `koin`.`new_koreatech_articles` (article_id, author, portal_num, portal_hit, url, registered_at, is_deleted,
                                             created_at, updated_at)
SELECT na.id,
       ka.author,
       ka.article_num,
       ka.hit,
       ka.url,
       ka.registered_at,
       ka.is_deleted,
       ka.created_at,
       ka.updated_at
FROM `koin`.`koreatech_articles` ka
         JOIN `koin`.`new_articles` na ON ka.id = na.old_id
ORDER BY ka.registered_at ASC, ka.id ASC;
