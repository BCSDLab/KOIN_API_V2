INSERT INTO timetable_frame (user_id, semester_id, name, is_main)
SELECT user_id, semester_id, '시간표1', 1
FROM timetables GROUP BY user_id, semester_id;
