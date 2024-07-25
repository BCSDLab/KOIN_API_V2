package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewImage;
import in.koreatech.koin.domain.shop.model.ShopReviewMenu;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.repository.ShopReviewImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewMenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.user.model.Student;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopReviewReportFixture {

    private final ShopReviewReportRepository shopReviewReportRepository;

    public ShopReviewReportFixture(
        ShopReviewReportRepository shopReviewReportRepository) {
        this.shopReviewReportRepository = shopReviewReportRepository;
    }

    public ShopReviewReport 리뷰_신고(Student student, ShopReview shopReview) {
        return shopReviewReportRepository.save(
            ShopReviewReport.builder()
                .review(shopReview)
                .title("기타")
                .content("부적절한 리뷰입니다.")
                .userId(student)
                .build()
        );
    }
}
