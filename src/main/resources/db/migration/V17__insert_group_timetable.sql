INSERT INTO group_timetable (`user_id`, `semester_id`, `name`, `is_main`)
SELECT `user_id`, `semester_id`, '메인시간표', 1
FROM timetables GROUP BY `user_id`, `semester_id`;
