package in.koreatech.koin.domain.timetableV3.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SemesterResponseV3(
    @Schema(description = "year", example = "2024", requiredMode = REQUIRED)
    Integer year,

    @Schema(description = "term", example = "1학기", requiredMode = REQUIRED)
    String term
) {
    public static SemesterResponseV3 of(Semester semester) {
        return new SemesterResponseV3(
            semester.getYear(),
            semester.getTerm().getDescription()
        );
    }
}
