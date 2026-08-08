package in.koreatech.koin.admin.semester.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV3.model.Term;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminTimetableSemesterCreateRequest(
    @Schema(description = "연도", example = "2026", requiredMode = REQUIRED)
    @NotNull(message = "연도는 필수입니다.")
    @Positive(message = "연도는 양수여야 합니다.")
    Integer year,

    @Schema(description = "학기", example = "FIRST", requiredMode = REQUIRED)
    @NotNull(message = "학기는 필수입니다.")
    Term term
) {

}
