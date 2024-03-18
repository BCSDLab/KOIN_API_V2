package in.koreatech.koin.domain.timetable.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimeTable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TimeTableResponse(
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
    public static TimeTableResponse from(TimeTable timeTable){
        return new TimeTableResponse(
            timeTable.getCode(),
            timeTable.getClassTitle(),
            toListClassTime(timeTable.getClassTime()),
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

    private static List<Integer> toListClassTime(String classTime) {
        classTime = classTime.substring(1, classTime.length() - 1);

        if (!classTime.isEmpty()) {
            List<String> numbers = List.of(classTime.split(", "));

            return numbers.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
}
