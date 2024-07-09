UPDATE timetable_lecture t
    JOIN timetable_frame f ON t.user_id = f.user_id AND t.semester_id = f.semester_id
    SET t.frame_id = f.id;
