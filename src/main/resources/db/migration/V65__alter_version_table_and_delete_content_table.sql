DROP TABLE `version_contents`;

ALTER TABLE `versions`
    ADD COLUMN `content` TEXT;
