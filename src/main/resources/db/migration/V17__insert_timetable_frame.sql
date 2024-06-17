INSERT INTO timetable_frame (user_id, semester_id, name, is_main, is_deleted)
SELECT user_id, semester_id, '시간표1', 1, is_deleted
FROM timetables GROUP BY user_id, semester_id, is_deleted;
