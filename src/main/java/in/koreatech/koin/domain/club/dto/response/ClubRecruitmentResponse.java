package in.koreatech.koin.domain.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubRecruitmentResponse(
    @Schema(description = "동아리 모집 고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "동아리 모집 상태", example = "RECRUITING", requiredMode = REQUIRED)
    String status,

    @Schema(description = "동아리 모집 디데이", example = "2", requiredMode = NOT_REQUIRED)
    Integer Dday,

    @Schema(description = "동아리 모집 시작일", example = "2025.06.09", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy.MM.dd")
    LocalDate startDate,

    @Schema(description = "동아리 모집 마감일", example = "2025.06.09", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy.MM.dd")
    LocalDate endDate,

    @Schema(description = "동아리 모집 이미지 url", example = "https://static.koreatech.in/test.png", requiredMode = NOT_REQUIRED)
    String imageUrl,

    @Schema(description = "동아리 모집 상세글", example = "BCSD 모집합니다.", requiredMode = NOT_REQUIRED)
    String content,

    @Schema(description = "동아리 관리자 여부", example = "true", requiredMode = REQUIRED)
    Boolean isManager
) {

}
