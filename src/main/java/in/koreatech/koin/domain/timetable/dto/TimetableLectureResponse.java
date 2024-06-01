package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureResponse(
    @Schema(description = "시간표 프레임 고유 번호", example = "1", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Schema(description = "강의시간표 상세 정보", requiredMode = REQUIRED)
    List<InnerTimetableLectureResponse> TimetableLecture,

    @Schema(name = "해당 학기 학점", example = "21")
    Integer grades,

    @Schema(name = "전체 학기 학점", example = "121")
    Integer totalGrades
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableLectureResponse(
        @Schema(description = "강의시간표 식별 번호", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "강의 이름", example = "기상분석", requiredMode = REQUIRED)
        String className,

        @Schema(description = "강의 시간", example = "[210, 211]", requiredMode = REQUIRED)
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "도서관", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(description = "교수명", example = "이강환", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "메모", example = "메모메모", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(description = "강의 고유 번호", example = "1", requiredMode = NOT_REQUIRED)
        Integer lectureId
    ) {
        public static List<InnerTimetableLectureResponse> from(List<TimetableLecture> timetableLectures) {
            List<InnerTimetableLectureResponse> timetableLectureList = new ArrayList<>();

            for(TimetableLecture timetableLecture : timetableLectures) {
                InnerTimetableLectureResponse response;
                if (timetableLecture.getLecture() == null) {
                    response = new InnerTimetableLectureResponse(
                        timetableLecture.getId(),
                        timetableLecture.getClassName(),
                        parseIntegerClassTimesFromString(timetableLecture.getClassTime()),
                        timetableLecture.getClassPlace(),
                        timetableLecture.getProfessor(),
                        timetableLecture.getMemo(),
                        null
                    );
                } else {
                    response = new InnerTimetableLectureResponse(
                        timetableLecture.getId(),
                        timetableLecture.getLecture().getName(),
                        parseIntegerClassTimesFromString(timetableLecture.getLecture().getClassTime()),
                        timetableLecture.getClassPlace(),
                        timetableLecture.getLecture().getProfessor(),
                        timetableLecture.getMemo(),
                        timetableLecture.getLecture().getId()
                    );
                }
                timetableLectureList.add(response);
            }
            return timetableLectureList;
        }
    }

    public static TimetableLectureResponse of(Integer timetableFrameId, List<TimetableLecture> timetableLectures, Integer grades, Integer totalGrades) {
        return new TimetableLectureResponse(timetableFrameId, InnerTimetableLectureResponse.from(timetableLectures), grades, totalGrades);
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
