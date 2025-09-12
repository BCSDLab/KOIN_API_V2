package in.koreatech.koin.domain.club.recruitment.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.lang.Boolean.TRUE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubRecruitmentCreateRequest(
    @Schema(description = "모집 시작 기간", example = "2025-07-01", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @Schema(description = "모집 마감 기간", example = "2025-07-02", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate,

    @Schema(description = "상시 모집 여부", example = "false", requiredMode = REQUIRED)
    Boolean isAlwaysRecruiting,

    @Schema(description = "모집 이미지", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = NOT_REQUIRED)
    String imageUrl,

    @Schema(description = "모집 상세 설명", example = "BCSD LAB 모집", requiredMode = NOT_REQUIRED)
    String content
) {
    public ClubRecruitmentCreateRequest {
        if (isAlwaysRecruiting && (startDate != null || endDate != null)) {
            throw CustomException.of(MUST_BE_NULL_RECRUITMENT_PERIOD);
        }

        if (!TRUE.equals(isAlwaysRecruiting)) {
            if (startDate == null || endDate == null) {
                throw CustomException.of(REQUIRED_RECRUITMENT_PERIOD);
            }
            if (endDate.isBefore(startDate)) {
                throw CustomException.of(INVALID_RECRUITMENT_PERIOD);
            }
        }
    }

    public ClubRecruitment toEntity(Club club) {
        return ClubRecruitment.builder()
            .startDate(startDate)
            .endDate(endDate)
            .isAlwaysRecruiting(isAlwaysRecruiting)
            .content(content)
            .imageUrl(imageUrl)
            .club(club)
            .build();
    }
}
