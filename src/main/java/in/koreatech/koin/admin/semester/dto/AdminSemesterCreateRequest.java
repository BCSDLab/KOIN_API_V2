package in.koreatech.koin.admin.semester.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminSemesterCreateRequest(
    @Schema(description = "학기 이름", example = "26-계절학기", requiredMode = REQUIRED)
    @NotBlank(message = "학기 이름은 필수 입력값입니다.")
    String semester,

    @Schema(description = "시작일", example = "2026-01-01", requiredMode = REQUIRED)
    @NotNull(message = "시작일은 필수 입력값입니다.")
    LocalDate fromDate,

    @Schema(description = "마감일", example = "2026-01-31", requiredMode = REQUIRED)
    @NotNull(message = "마감일은 필수 입력값입니다.")
    LocalDate toDate
) {
    public CoopSemester toEntity() {
        return CoopSemester.builder()
            .semester(semester)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();
    }
}
