package in.koreatech.koin.domain.timetable.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LectureResponse(

    @Schema(name = "과목 코드", example = "ARB244")
    String code,

    @Schema(name = "과목 이름", example = "건축구조의 이해 및 실습")
    String name,

    @Schema(name = "대상 학년", example = "3")
    String grades,

    @Schema(name = "분반", example = "01")
    String lectureClass,

    @Schema(name = "수강 인원", example = "25")
    String regularNumber,

    @Schema(name = "학부", example = "디자인ㆍ건축공학부")
    String department,

    @Schema(name = "대상", example = "디자 1 건축")
    String target,

    @Schema(name = "강의 교수", example = "이돈우")
    String professor,

    @Schema(name = "영어 수업인지", example = "N")
    String isEnglish,

    @Schema(name = "설계 학점", example = "0")
    String designScore,

    @Schema(name = "이러닝인지", example = "Y")
    String isElearning,

    @Schema(name = "강의 시간", example = "[200,201,202,203,204,205,206,207]")
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
        classTime = classTime.substring(1, classTime.length() - 1);
        List<String> numbers = List.of(classTime.split(","));

        return numbers.stream()
            .map(String::trim)
            .map(s -> s.replace(" ", ""))
            .map(Long::parseLong)
            .toList();
    }
}
