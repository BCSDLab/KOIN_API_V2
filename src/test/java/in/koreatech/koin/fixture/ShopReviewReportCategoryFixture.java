package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.user.model.Student;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopReviewReportCategoryFixture {

    private final ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;

    public ShopReviewReportCategoryFixture(
        ShopReviewReportCategoryRepository shopReviewReportCategoryRepository) {
        this.shopReviewReportCategoryRepository = shopReviewReportCategoryRepository;
    }

    public ShopReviewReportCategory 리뷰_신고_카테고리_1() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("주제에 맞지 않음")
                .detail("해당 음식점과 관련 없는 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_카테고리_2() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("스팸")
                .detail("광고가 포함된 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_카테고리_3() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("욕설")
                .detail("욕설, 성적인 언어, 비방하는 글이 포함된 리뷰입니다.")
                .build()
        );
    }

    public ShopReviewReportCategory 리뷰_신고_카테고리_4() {
        return shopReviewReportCategoryRepository.save(
            ShopReviewReportCategory.builder()
                .name("기타")
                .detail("")
                .build()
        );
    }
}
