package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record ProfileActivityInput(
    @Schema(description = "활동명", example = "AI 공모전", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    String title,

    @Schema(description = "시작일", example = "2025-03-03", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    LocalDate startedAt,

    @Schema(description = "종료일. 진행 중인 활동이면 null이고, 그 외에는 시작일과 같거나 이후여야 합니다.", example = "2025-05-05",
        nullable = true, requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endedAt,

    @Schema(description = "진행 중 여부", example = "false", requiredMode = REQUIRED)
    @NotNull
    Boolean isOngoing,

    @Schema(description = "활동 설명", example = "기획 담당", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 500)
    String description
) {
    public ProfileActivityInput {
        if (TRUE.equals(isOngoing) && endedAt != null) {
            throw CustomException.of(TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL);
        }
        if (FALSE.equals(isOngoing)) {
            if (endedAt == null) {
                throw CustomException.of(TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED);
            }
            if (startedAt != null && endedAt.isBefore(startedAt)) {
                throw CustomException.of(INVALID_START_DATE_AFTER_END_DATE);
            }
        }
    }
}
