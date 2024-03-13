package in.koreatech.koin.domain.TimeTable.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.TimeTable.model.Semester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SemesterResponse(
    @Schema(description = "id", example = "1")
    Long id,
    @Schema(description = "학기", example = "20241")
    String semester
) {
    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
            semester.getId(),
            semester.getSemester()
        );
    }
}
