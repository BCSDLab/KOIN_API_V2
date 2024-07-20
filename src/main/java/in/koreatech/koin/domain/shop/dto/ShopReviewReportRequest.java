package in.koreatech.koin.domain.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopReviewReportRequest (
    @Schema(example = "기타", description = "신고 제목", requiredMode = REQUIRED)
    @NotBlank(message = "신고 제목은 필수입니다.")
    String title,

    @Schema(example = "이 글은 논란이 될수도 있겠는데요~?", description = "신고 내용", requiredMode = REQUIRED)
    @NotBlank(message = "신고 내용은 필수입니다.")
    String content
){
}
