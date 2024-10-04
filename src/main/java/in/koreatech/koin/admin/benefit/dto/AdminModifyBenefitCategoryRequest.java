package in.koreatech.koin.admin.benefit.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminModifyBenefitCategoryRequest(
    @Schema(description = "혜택 카테고리 제목", example = "배달비 아끼기", requiredMode = REQUIRED)
    @NotBlank(message = "혜택 카테고리 제목은 필수입니다.")
    String title,

    @Schema(description = "혜택 카테고리 설명", example = "계좌이체하면 배달비가 무료(할인)인 상점들을 모아뒀어요.", requiredMode = REQUIRED)
    @NotBlank(message = "혜택 카테고리 설명은 필수입니다.")
    String detail,

    @Schema(description = "혜택 카테고리 ON 이미지 URL", example = "https://example.com/button_on.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "혜택 카테고리 ON 이미지 URL은 필수입니다.")
    String onImageUrl,

    @Schema(description = "혜택 카테고리 OFF 이미지 URL", example = "https://example.com/button_off.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "혜택 카테고리 OFF 이미지 URL은 필수입니다.")
    String offImageUrl
) {

    public BenefitCategory toBenefitCategory() {
        return BenefitCategory.builder()
            .title(title)
            .detail(detail)
            .onImageUrl(onImageUrl)
            .offImageUrl(offImageUrl)
            .build();
    }
}
