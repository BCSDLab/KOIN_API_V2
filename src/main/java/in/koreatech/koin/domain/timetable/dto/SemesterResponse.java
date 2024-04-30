package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SemesterResponse(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "학기", example = "20241", requiredMode = REQUIRED)
    String semester
) {

    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
            semester.getId(),
            semester.getSemester()
        );
    }
}
