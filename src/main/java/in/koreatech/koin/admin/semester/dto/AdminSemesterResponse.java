package in.koreatech.koin.admin.semester.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminSemesterResponse(
    @Schema(description = "학기 고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "운영 학기", example = "24-2학기", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "학기 기간 시작일", example = "2024-09-02", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fromDate,

    @Schema(description = "학기 기간 마감일", example = "2024-12-20", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate toDate,

    @Schema(description = "현재 적용 학기 여부", example = "true", requiredMode = REQUIRED)
    Boolean isApplied
) {
    public static AdminSemesterResponse from(CoopSemester semester) {
        return new AdminSemesterResponse(
            semester.getId(),
            semester.getSemester(),
            semester.getFromDate(),
            semester.getToDate(),
            semester.isApplied()
        );
    }
}
