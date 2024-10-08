package in.koreatech.koin.admin.benefit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminModifyBenefitCategoryResponse(
    @Schema(description = "혜택 카테고리 ID", example = "1")
    Integer id,

    @Schema(description = "혜택 카테고리 제목", example = "배달비 아끼기")
    String title,

    @Schema(description = "혜택 카테고리 설명", example = "계좌이체하면 배달비가 무료(할인)인 상점들을 모아뒀어요.")
    String detail,

    @Schema(description = "혜택 카테고리 ON 이미지 URL", example = "https://example.com/button_on.jpg")
    String onImageUrl,

    @Schema(description = "혜택 카테고리 OFF 이미지 URL", example = "https://example.com/button_off.jpg")
    String offImageUrl
) {

    public static AdminModifyBenefitCategoryResponse from(BenefitCategory benefitCategory) {
        return new AdminModifyBenefitCategoryResponse(
            benefitCategory.getId(),
            benefitCategory.getTitle(),
            benefitCategory.getDetail(),
            benefitCategory.getOnImageUrl(),
            benefitCategory.getOffImageUrl()
        );
    }
}
