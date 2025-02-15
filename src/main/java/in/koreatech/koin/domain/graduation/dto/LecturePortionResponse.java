package in.koreatech.koin.domain.graduation.dto;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

public record LecturePortionResponse(
    @Schema(description = "강의 ID", example = "1")
    Integer id,

    @Schema(description = "강의 코드", example = "ABC123")
    String code,

    @Schema(description = "강의 이름", example = "컴퓨터구조")
    String name,

    @Schema(description = "학점", example = "3")
    String grades,

    @Schema(description = "학과", example = "컴퓨터공학부")
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
