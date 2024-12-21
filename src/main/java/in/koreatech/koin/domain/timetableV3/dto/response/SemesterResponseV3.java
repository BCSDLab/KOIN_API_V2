package in.koreatech.koin.domain.timetableV3.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV3.model.SemesterV3;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SemesterResponseV3(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "학기", example = "20241", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "year", example = "2024", requiredMode = REQUIRED)
    Integer year,

    @Schema(description = "term", example = "1학기", requiredMode = REQUIRED)
    String term
) {
    public static SemesterResponseV3 from(SemesterV3 semesterV3) {
        return new SemesterResponseV3(
            semesterV3.getId(),
            semesterV3.getSemester(),
            semesterV3.getYear(),
            semesterV3.getTerm().getDescription()
        );
    }
}
