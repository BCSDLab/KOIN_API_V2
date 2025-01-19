package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

public record CourseTypeLectureResponse(
    @Schema(description = "학기", example = "20192")
    String semester,

    @Schema(description = "이수구분 충족강의")
    List<Lecture> lectures
) {
    public static CourseTypeLectureResponse of(String semester, List<Lecture> lectures) {
        return new CourseTypeLectureResponse(semester, lectures);
    }
}
