package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableResponse(
    @Schema(description = "학기", example = "20241", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "시간표 상세정보")
    List<InnerTimetableResponse> timetable,

    @Schema(description = "해당 학기 학점", example = "21")
    Integer grades,

    @Schema(description = "전체 학기 학점", example = "121")
    Integer totalGrades
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableResponse(
        @Schema(description = "시간표 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "수강 정원", example = "40", requiredMode = NOT_REQUIRED)
        String regularNumber,

        @Schema(description = "과목 코드", example = "ARB244", requiredMode = NOT_REQUIRED)
        String code,

        @Schema(description = "설계 학점", example = "0", requiredMode = NOT_REQUIRED)
        String designScore,

        @Schema(description = "강의(커스텀) 시간", example = "[204, 205, 206, 207, 302, 303]", requiredMode = REQUIRED)
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "2 공학관", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(description = "메모", example = "null", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(description = "학점", example = "3", requiredMode = REQUIRED)
        String grades,

        @Schema(description = "강의(커스텀) 이름", example = "한국사", requiredMode = REQUIRED)
        String classTitle,

        @Schema(description = "분반", example = "01", requiredMode = NOT_REQUIRED)
        String lectureClass,

        @Schema(description = "대상", example = "디자 1 건축", requiredMode = NOT_REQUIRED)
        String target,

        @Schema(description = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "학부", example = "디자인ㆍ건축공학부", requiredMode = NOT_REQUIRED)
        String department
    ) {

        public static List<InnerTimetableResponse> from(List<TimetableLecture> timetableLectures) {
            return timetableLectures.stream()
                .map(timeTableLecture -> {
                    if (timeTableLecture.getLecture() == null) {
                        return new InnerTimetableResponse(
                                timeTableLecture.getId(),
                                null,
                                null,
                                null,
                                parseIntegerClassTimesFromString(timeTableLecture.getClassTime()),
                                timeTableLecture.getClassPlace(),
                                timeTableLecture.getMemo(),
                                null,
                                timeTableLecture.getClassTitle(),
                                null,
                                null,
                                timeTableLecture.getProfessor(),
                                null
                        );
                    } else {
                        return new InnerTimetableResponse(
                            timeTableLecture.getId(),
                            timeTableLecture.getLecture().getRegularNumber(),
                            timeTableLecture.getLecture().getCode(),
                            timeTableLecture.getLecture().getDesignScore(),
                            parseIntegerClassTimesFromString(timeTableLecture.getLecture().getClassTime()),
                            timeTableLecture.getClassPlace(),
                            timeTableLecture.getMemo(),
                            timeTableLecture.getLecture().getGrades(),
                            timeTableLecture.getLecture().getName(),
                            timeTableLecture.getLecture().getLectureClass(),
                            timeTableLecture.getLecture().getTarget(),
                            timeTableLecture.getLecture().getProfessor(),
                            timeTableLecture.getLecture().getDepartment()
                        );
                    }
                })
                .toList();
        }
    }

    public static TimetableResponse of(List<TimetableLecture> timetableLectures, TimetableFrame timetableFrame,
        Integer grades, Integer totalGrades) {
        return new TimetableResponse(
            timetableFrame.getSemester().getSemester(),
            InnerTimetableResponse.from(timetableLectures),
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
