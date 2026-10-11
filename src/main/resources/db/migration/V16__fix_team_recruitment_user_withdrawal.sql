ALTER TABLE `team_recruitment`
    DROP FOREIGN KEY `fk_team_recruitment_author_id`,
    MODIFY COLUMN `author_id` INT UNSIGNED DEFAULT NULL,
    ADD CONSTRAINT `fk_team_recruitment_author_id_withdrawal`
        FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `team_recruitment_application`
    DROP FOREIGN KEY `fk_team_recruitment_application_applicant_id`,
    DROP CHECK `chk_team_recruitment_application_status`,
    MODIFY COLUMN `applicant_id` INT UNSIGNED DEFAULT NULL,
    ADD CONSTRAINT `fk_team_recruitment_application_applicant_id_withdrawal`
        FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    ADD CONSTRAINT `chk_team_recruitment_application_status_withdrawal`
        CHECK (`status` IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'));

ALTER TABLE `team_recruitment_chat_message`
    DROP FOREIGN KEY `fk_team_recruitment_chat_message_sender_id`,
    MODIFY COLUMN `sender_id` INT UNSIGNED DEFAULT NULL,
    ADD CONSTRAINT `fk_team_recruitment_chat_message_sender_id_withdrawal`
        FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `team_recruitment_chat_member`
    DROP FOREIGN KEY `fk_team_recruitment_chat_member_user_id`,
    ADD CONSTRAINT `fk_team_recruitment_chat_member_user_id_withdrawal`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `team_recruitment_notification`
    DROP FOREIGN KEY `fk_team_recruitment_notification_recipient_id`,
    ADD CONSTRAINT `fk_team_recruitment_notification_recipient_id_withdrawal`
        FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
