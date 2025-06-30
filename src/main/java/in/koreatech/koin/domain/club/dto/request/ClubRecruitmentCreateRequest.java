package in.koreatech.koin.domain.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin._common.exception.errorcode.ErrorCode.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.lang.Boolean.TRUE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubRecruitment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubRecruitmentCreateRequest(
    @Schema(description = "모집 시작 기간", example = "2025-07-01", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startData,

    @Schema(description = "모집 마감 기간", example = "2025-07-02", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endData,

    @Schema(description = "상시 모집 여부", example = "false", requiredMode = REQUIRED)
    Boolean isAlwaysRecruiting,

    @Schema(description = "모집 상세 설명", example = "BCSD LAB 모집", requiredMode = REQUIRED)
    @NotNull(message = "모집 상세 설명은 필수 입력입니다.")
    String content
) {
    public ClubRecruitmentCreateRequest {
        if (isAlwaysRecruiting && (startData != null || endData != null)) {
            throw CustomException.of(MUST_BE_NULL_RECRUITMENT_PERIOD);
        }

        if (!TRUE.equals(isAlwaysRecruiting)) {
            if (startData == null || endData == null) {
                throw CustomException.of(REQUIRED_RECRUITMENT_PERIOD);
            }
            if (endData.isBefore(startData)) {
                throw CustomException.of(INVALID_RECRUITMENT_PERIOD);
            }
        }
    }

    public ClubRecruitment toEntity(Club club) {
        return ClubRecruitment.builder()
            .startDate(startData)
            .endDate(endData)
            .isAlwaysRecruiting(isAlwaysRecruiting)
            .content(content)
            .club(club)
            .build();
    }
}
