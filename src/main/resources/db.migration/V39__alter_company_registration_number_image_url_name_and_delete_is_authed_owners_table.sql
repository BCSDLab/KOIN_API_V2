ALTER TABLE `koin`.`owners`
DROP COLUMN `is_authed`,
CHANGE COLUMN `company_registration_number_image_url` `company_registration_certificate_image_url` VARCHAR(255) NOT NULL ;