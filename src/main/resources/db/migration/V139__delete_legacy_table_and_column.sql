DROP TABLE IF EXISTS users_owners;
DROP TABLE IF EXISTS koreatech_articles;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS bus_timetables;
DROP TABLE IF EXISTS timetables;

TRUNCATE TABLE comments;

ALTER TABLE users DROP COLUMN reset_token;
ALTER TABLE users DROP COLUMN reset_expired_at;
