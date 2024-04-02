package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimeTable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimeTableResponse(
    @Schema(name = "학기", example = "20241")
    String semester,

    @Schema(name = "시간표 상세정보")
    List<InnerTimeTableResponse> timetable
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableResponse(
        @Schema(name = "시간표 ID", example = "1")
        Long id,

        @Schema(name = "과목 코드", example = "ARB244")
        String regularNumber,

        @Schema(name = "과목 코드", example = "ARB244")
        String code,

        @Schema(description = "설계 학점", example = "0")
        String designScore,

        @Schema(description = "강의 시간", example = "[204, 205, 206, 207, 302, 303]")
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "null")
        String classPlace,

        @Schema(description = "메모", example = "null", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(name = "대상 학년", example = "3")
        String grades,

        @Schema(name = "강의 이름", example = "한국사")
        String classTitle,

        @Schema(name = "분반", example = "01")
        String lectureClass,

        @Schema(name = "대상", example = "디자 1 건축")
        String target,

        @Schema(name = "강의 교수", example = "이돈우")
        String professor,

        @Schema(name = "학부", example = "디자인ㆍ건축공학부")
        String department
    ) {

        public static List<InnerTimeTableResponse> from(List<TimeTable> timeTables) {
            return timeTables.stream()
                .map(it -> new InnerTimeTableResponse(
                        it.getId(),
                        it.getRegularNumber(),
                        it.getCode(),
                        it.getDesignScore(),
                        parseIntegerClassTimesFromString(it.getClassTime()),
                        it.getClassPlace(),
                        it.getMemo(),
                        it.getGrades(),
                        it.getClassTitle(),
                        it.getLectureClass(),
                        it.getTarget(),
                        it.getProfessor(),
                        it.getDepartment()
                    )
                )
                .toList();
        }

    }

    public static TimeTableResponse of(String semester, List<TimeTable> timeTables) {
        return new TimeTableResponse(
            semester,
            InnerTimeTableResponse.from(timeTables)
        );
    }

    private static final int INITIAL_BRACE_INDEX = 1;

    private static List<Integer> parseIntegerClassTimesFromString(String classTime) {
        String classTimeWithoutBrackets = classTime.substring(INITIAL_BRACE_INDEX, classTime.length() - 1);

        if (!classTimeWithoutBrackets.isEmpty()) {
            return Arrays.stream(classTimeWithoutBrackets.split(", "))
                .map(Integer::parseInt)
                .toList();
        } else {
            return Collections.emptyList();
        }
    }
}
