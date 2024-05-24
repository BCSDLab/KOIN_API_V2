UPDATE `timetables` t
    JOIN `group_timetable` gt ON t.user_id = gt.user_id AND t.semester_id = gt.semester_id
    SET t.group_timetable_id = gt.id;

ALTER TABLE `timetables` ADD CONSTRAINT `FK_TIMETABLE_ON_GROUP_TIMETABLE FOREIGN KEY`
    FOREIGN KEY (`group_timetable_id`) REFERENCES group_timetable(`id`);
