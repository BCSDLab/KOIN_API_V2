package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimeTable;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SemesterCheckResponse
    (
        @Schema(description = "유저 id", example = "1", requiredMode = REQUIRED)
        Integer userId,

        @Schema(description = "유저 학기", example = "[20191, 20192]", requiredMode = REQUIRED)
        List<String> semesters
    ) {

    public static SemesterCheckResponse of(Integer userId, List<TimeTable> timetables) {
        List<String> semesters = timetables.stream()
            .map(timeTable -> timeTable.getSemester().getSemester())
            .distinct()
            .toList();

        return new SemesterCheckResponse(
            userId,
            semesters
        );
    }
}
