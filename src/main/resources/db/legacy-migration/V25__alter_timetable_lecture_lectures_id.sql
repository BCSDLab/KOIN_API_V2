UPDATE timetable_lecture tl
    JOIN timetables t ON tl.id = t.id
    JOIN lectures l ON t.class_title = l.name
    AND t.class_time = l.class_time
    JOIN semester s ON t.semester_id = s.id
    SET tl.lectures_id = l.id;
