package in.koreatech.koin.domain.course_registration.dto;

import static in.koreatech.koin.domain.course_registration.util.CourseClassTimeParser.extractRawClassTime;
import static in.koreatech.koin.domain.course_registration.util.CourseClassTimeParser.parseClassTime;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CourseRegistrationLectureResponse(

    @Schema(description = "학과", example = "기계공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "과목 정보", requiredMode = REQUIRED)
    InnerLectureInfo lectureInfo,

    @Schema(description = "분반", example = "01", requiredMode = REQUIRED)
    String classNumber,

    @Schema(description = "학점", example = "3", requiredMode = REQUIRED)
    String grades,

    @Schema(description = "교수명", example = "홍길동", requiredMode = REQUIRED)
    String professor,

    @Schema(description = "수강 정원", example = "38", requiredMode = REQUIRED)
    String regularNumber,

    @Schema(description = "수업 교시", example = "월01A~03B,화01A~03B", requiredMode = REQUIRED)
    String classTime,

    @Schema(description = "수업 교시 원본 데이터", example = "[0,1,2,3,4,5,100,101,102,103,104,105]", requiredMode = REQUIRED)
    List<Integer> classTimeRaw
) {

    public static CourseRegistrationLectureResponse from(Lecture lecture) {
        List<Integer> classTimeRaw = extractRawClassTime(lecture.getClassTime());

        return new CourseRegistrationLectureResponse(
            lecture.getDepartment(),
            new InnerLectureInfo(lecture.getCode(), lecture.getName()),
            lecture.getLectureClass(),
            lecture.getGrades(),
            lecture.getProfessor(),
            lecture.getRegularNumber(),
            parseClassTime(classTimeRaw),
            classTimeRaw
        );
    }

    public static List<CourseRegistrationLectureResponse> fromList(List<Lecture> lectures) {
        return lectures.stream()
            .map(CourseRegistrationLectureResponse::from)
            .collect(Collectors.toList());
    }
}
