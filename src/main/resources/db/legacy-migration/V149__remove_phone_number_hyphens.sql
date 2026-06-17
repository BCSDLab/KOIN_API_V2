UPDATE koin.users
SET phone_number = REPLACE(phone_number, '-', '')
WHERE phone_number IS NOT NULL;
