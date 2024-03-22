INSERT INTO students
    (user_id, anonymous_nickname, student_number, major, is_graduated, auth_token, auth_expired_at, reset_token, reset_expired_at)
SELECT id, anonymous_nickname, student_number, major, is_graduated, auth_token, auth_expired_at, reset_token, reset_expired_at
from users;