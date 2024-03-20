package in.koreatech.koin.domain.timetable.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimeTable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimeTableResponse(
    @Schema(name = "시간표 번호", example = "1")
    Long id,

    @Schema(name = "과목 코드", example = "ARB244")
    String code,

    @Schema(description = "강의 이름", example = "한국사")
    String classTitle,

    @Schema(description = "강의 시간", example = "[204, 205, 206, 207, 302, 303]")
    List<Integer> classTime,

    @Schema(description = "강의 장소", example = "null")
    String classPlace,

    @Schema(name = "강의 교수", example = "이돈우")
    String professor,

    @Schema(name = "대상 학년", example = "3")
    String grades,

    @Schema(name = "분반", example = "01")
    String lectureClass,

    @Schema(name = "대상", example = "디자 1 건축")
    String target,

    @Schema(name = "수강 인원", example = "25")
    String regularNumber,

    @Schema(name = "설계 학점", example = "0")
    String designScore,

    @Schema(name = "학부", example = "디자인ㆍ건축공학부")
    String department,

    @Schema(name = "memo", example = "null")
    String memo
) {

    private static final int INITIAL_BRACE_INDEX = 1;

    public static TimeTableResponse from(TimeTable timeTable) {
        return new TimeTableResponse(
            timeTable.getId(),
            timeTable.getCode(),
            timeTable.getClassTitle(),
            parseIntegerClassTimesFromString(timeTable.getClassTime()),
            timeTable.getClassPlace(),
            timeTable.getProfessor(),
            timeTable.getGrades(),
            timeTable.getLectureClass(),
            timeTable.getTarget(),
            timeTable.getRegularNumber(),
            timeTable.getDesignScore(),
            timeTable.getDepartment(),
            timeTable.getMemo()
        );
    }

    private static List<Integer> parseIntegerClassTimesFromString(String classTime) {
        String classTimeWithoutBrackets = classTime.substring(INITIAL_BRACE_INDEX,
            classTime.length() - INITIAL_BRACE_INDEX);

        if (!classTimeWithoutBrackets.isEmpty()) {
            return Arrays.stream(classTimeWithoutBrackets.split(", "))
                .map(Integer::parseInt)
                .toList();
        } else {
            return Collections.emptyList();
        }
    }
}
