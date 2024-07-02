INSERT INTO timetable_lecture (class_place, memo, is_deleted, user_id, semester_id)
SELECT t.class_place, t.memo, is_deleted, t.user_id, t.semester_id
FROM timetables t;
