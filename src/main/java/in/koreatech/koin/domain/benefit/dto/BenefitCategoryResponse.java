package in.koreatech.koin.domain.benefit.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.List;

import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record BenefitCategoryResponse(
    @Schema(description = "혜택 카테고리 리스트", requiredMode = NOT_REQUIRED)
    List<InnerBenefitResponse> benefits

) {
    public static BenefitCategoryResponse from(List<BenefitCategory> benefitCategories) {
        return new BenefitCategoryResponse(
            benefitCategories.stream().map(InnerBenefitResponse::from).toList()
        );
    }

    public record InnerBenefitResponse(
        @Schema(description = "혜택 id", example = "1", requiredMode = NOT_REQUIRED)
        Integer id,

        @Schema(description = "혜택 카테고리 제목", example = "배달비 아끼기", requiredMode = NOT_REQUIRED)
        String title,

        @Schema(description = "혜택 카테고리 설명", example = "계좌이체하면 배달비가 무료(할인)인 상점들만 모아뒀어요.", requiredMode = NOT_REQUIRED)
        String detail
    ) {
        public static InnerBenefitResponse from(BenefitCategory benefitCategory) {
            return new InnerBenefitResponse(
                benefitCategory.getId(),
                benefitCategory.getTitle(),
                benefitCategory.getDetail()
            );
        }
    }
}
