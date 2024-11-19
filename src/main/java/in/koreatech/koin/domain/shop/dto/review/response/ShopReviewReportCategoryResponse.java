package in.koreatech.koin.domain.shop.dto.review.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import in.koreatech.koin.domain.shop.model.review.ShopReviewReportCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record ShopReviewReportCategoryResponse(
    @Schema(description = "리뷰 신고 카테고리 개수", example = "2", requiredMode = REQUIRED)
    int count,

    @Schema(description = "리뷰 신고 카테고리", requiredMode = REQUIRED)
    List<InnerReportCategory> categories
) {
    public static ShopReviewReportCategoryResponse from(List<ShopReviewReportCategory> shopReviewReportCategories) {
        return new ShopReviewReportCategoryResponse(
            shopReviewReportCategories.size(),
            shopReviewReportCategories.stream().map(InnerReportCategory::from).toList()
        );
    }

    public record InnerReportCategory(
        @Schema(description = "리뷰 신고 카테고리 이름", example = "주제에 맞지 않음", requiredMode = REQUIRED)
        String name,

        @Schema(description = "리뷰 신고 카테고리 내용", example = "해당 음식점과 관련 없는 리뷰입니다.", requiredMode = REQUIRED)
        String detail
    ) {
        public static InnerReportCategory from(ShopReviewReportCategory review) {
            return new InnerReportCategory(
                review.getName(),
                review.getDetail()
            );
        }
    }
}
