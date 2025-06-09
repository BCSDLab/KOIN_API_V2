package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.review.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.repository.review.ShopReviewReportCategoryRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopReviewReportCategoryFixture {

    private final ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;

    public ShopReviewReportCategoryFixture(
        ShopReviewReportCategoryRepository shopReviewReportCategoryRepository) {
        this.shopReviewReportCategoryRepository = shopReviewReportCategoryRepository;
    }

    public ShopReviewReportCategory 리뷰_신고_주제에_맞지_않음() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("주제에 맞지 않음")
                .detail("해당 음식점과 관련 없는 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_스팸() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("스팸")
                .detail("광고가 포함된 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_욕설() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("욕설")
                .detail("욕설, 성적인 언어, 비방하는 글이 포함된 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_기타() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("기타")
                .detail("")
                .build()
        );
    }
}
