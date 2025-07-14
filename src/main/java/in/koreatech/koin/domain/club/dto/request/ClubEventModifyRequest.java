package in.koreatech.koin.domain.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.validation.UniqueUrl;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubEventModifyRequest(
    @Schema(description = "행사 이름", example = "B-CON", requiredMode = REQUIRED)
    @NotBlank(message = "행사 이름은 필수입니다.")
    @Size(max = 30, message = "행사 이름은 30자 이내여야 합니다.")
    String name,

    @Schema(description = "행사 이미지 URL 리스트", example = """
        ["https://image1.com", "https://image2.com"]
        """, requiredMode = NOT_REQUIRED)
    @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
    List<String> imageUrls,

    @Schema(description = "행사 시작일", example = "2025-07-01T09:00:00", requiredMode = REQUIRED)
    @NotNull(message = "행사 시작일은 필수입니다.")
    LocalDateTime startDate,

    @Schema(description = "행사 종료일", example = "2025-07-02T18:00:00", requiredMode = REQUIRED)
    @NotNull(message = "행사 종료일은 필수입니다.")
    LocalDateTime endDate,

    @Schema(description = "행사 소개 (요약)", example = "BCSDLab의 멘토 혹은 레귤러들의 경험을 공유해요.", requiredMode = REQUIRED)
    @NotBlank(message = "행사 소개는 필수입니다.")
    @Size(max = 70, message = "소개는 70자 이내여야 합니다.")
    String introduce,

    @Schema(description = "행사 상세 내용", example = "여러 동아리원들과 자신의 생각, 경험에 대해 나눠요,", requiredMode = NOT_REQUIRED)
    String content
) {
}
