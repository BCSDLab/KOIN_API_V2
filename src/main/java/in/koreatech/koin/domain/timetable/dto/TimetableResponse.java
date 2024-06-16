package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableResponse(
    @Schema(name = "학기", example = "20241", requiredMode = REQUIRED)
    String semester,

    @Schema(name = "시간표 상세정보")
    List<InnerTimeTableResponse> timetable,

    @Schema(name = "해당 학기 학점", example = "21")
    Integer grades,

    @Schema(name = "전체 학기 학점", example = "121")
    Integer totalGrades
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableResponse(
        @Schema(name = "id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(name = "과목 코드", example = "ARB244", requiredMode = NOT_REQUIRED)
        String regularNumber,

        @Schema(name = "과목 코드", example = "ARB244", requiredMode = NOT_REQUIRED)
        String code,

        @Schema(description = "설계 학점", example = "0", requiredMode = NOT_REQUIRED)
        String designScore,

        @Schema(description = "강의(커스텀) 시간", example = "[204, 205, 206, 207, 302, 303]", requiredMode = REQUIRED)
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "2 공학관", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(description = "메모", example = "null", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(name = "학점", example = "3", requiredMode = REQUIRED)
        String grades,

        @Schema(name = "강의(커스텀) 이름", example = "한국사", requiredMode = REQUIRED)
        String classTitle,

        @Schema(name = "분반", example = "01", requiredMode = NOT_REQUIRED)
        String lectureClass,

        @Schema(name = "대상", example = "디자 1 건축", requiredMode = NOT_REQUIRED)
        String target,

        @Schema(name = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(name = "학부", example = "디자인ㆍ건축공학부", requiredMode = NOT_REQUIRED)
        String department
    ) {

        public static List<InnerTimeTableResponse> from(List<TimetableLecture> timeTableLectures) {
            return timeTableLectures.stream()
                .map(timeTableLecture -> new InnerTimeTableResponse(
                        timeTableLecture.getId(),
                        timeTableLecture.getLecture().getRegularNumber(),
                        timeTableLecture.getLecture().getCode(),
                        timeTableLecture.getLecture().getDesignScore(),
                        parseIntegerClassTimesFromString(timeTableLecture.getClassTime()),
                        timeTableLecture.getClassPlace(),
                        timeTableLecture.getMemo(),
                        timeTableLecture.getLecture().getGrades(),
                        timeTableLecture.getClassName(),
                        timeTableLecture.getLecture().getLectureClass(),
                        timeTableLecture.getLecture().getTarget(),
                        timeTableLecture.getProfessor(),
                        timeTableLecture.getLecture().getDepartment()
                    )
                )
                .toList();
        }
    }

    public static TimetableResponse of(List<TimetableLecture> timeTableLectures, TimetableFrame timeTableFrame,
        Integer grades, Integer totalGrades) {
        return new TimetableResponse(
            timeTableFrame.getSemester().getSemester(),
            InnerTimeTableResponse.from(timeTableLectures),
            grades,
            totalGrades
        );
    }

    private static final int INITIAL_BRACE_INDEX = 1;

    private static List<Integer> parseIntegerClassTimesFromString(String classTime) {
        String classTimeWithoutBrackets = classTime.substring(INITIAL_BRACE_INDEX, classTime.length() - 1);

        if (!classTimeWithoutBrackets.isEmpty()) {
            return Arrays.stream(classTimeWithoutBrackets.split(","))
                .map(String::strip)
                .map(Integer::parseInt)
                .toList();
        } else {
            return Collections.emptyList();
        }
    }
}
