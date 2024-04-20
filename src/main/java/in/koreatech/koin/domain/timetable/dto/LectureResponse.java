package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LectureResponse(

    @Schema(name = "과목 코드", example = "ARB244", requiredMode = REQUIRED)
    String code,

    @Schema(name = "과목 이름", example = "건축구조의 이해 및 실습", requiredMode = REQUIRED)
    String name,

    @Schema(name = "대상 학년", example = "3", requiredMode = REQUIRED)
    String grades,

    @Schema(name = "분반", example = "01", requiredMode = REQUIRED)
    String lectureClass,

    @Schema(name = "수강 인원", example = "25", requiredMode = NOT_REQUIRED)
    String regularNumber,

    @Schema(name = "학부", example = "디자인ㆍ건축공학부", requiredMode = REQUIRED)
    String department,

    @Schema(name = "대상", example = "디자 1 건축", requiredMode = REQUIRED)
    String target,

    @Schema(name = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
    String professor,

    @Schema(name = "영어 수업인지", example = "N", requiredMode = REQUIRED)
    String isEnglish,

    @Schema(name = "설계 학점", example = "0", requiredMode = REQUIRED)
    String designScore,

    @Schema(name = "이러닝인지", example = "Y", requiredMode = REQUIRED)
    String isElearning,

    @Schema(name = "강의 시간", example = "[200,201,202,203,204,205,206,207]", requiredMode = REQUIRED)
    List<Long> classTime
) {

    public static LectureResponse from(Lecture lecture) {
        return new LectureResponse(
            lecture.getCode(),
            lecture.getName(),
            lecture.getGrades(),
            lecture.getLectureClass(),
            lecture.getRegularNumber(),
            lecture.getDepartment(),
            lecture.getTarget(),
            lecture.getProfessor(),
            lecture.getIsEnglish(),
            lecture.getDesignScore(),
            lecture.getIsElearning(),
            toListClassTime(lecture.getClassTime())
        );
    }

    public static List<Long> toListClassTime(String classTime) {
        if ("[]".equals(classTime)) {
            return Collections.emptyList();
        }

        classTime = classTime.substring(1, classTime.length() - 1);
        List<String> numbers = List.of(classTime.split(","));

        return numbers.stream()
            .map(String::strip)
            .map(Long::parseLong)
            .toList();
    }
}
