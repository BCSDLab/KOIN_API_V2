package in.koreatech.koin.domain.graduation.dto;

import in.koreatech.koin.domain.timetable.model.Lecture;

public record LecturePortionResponse
    (
        Integer id,
        String code,
        String name,
        String grades,
        String department
    ) {
    public static LecturePortionResponse from(Lecture lecture) {
        return new LecturePortionResponse(
            lecture.getId(),
            lecture.getCode(),
            lecture.getName(),
            lecture.getGrades(),
            lecture.getDepartment()
        );
    }
}
