package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.domain.shop.repository.review.ShopReviewReportRepository;
import in.koreatech.koin.domain.student.model.Student;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class ShopReviewReportFixture {

    private final ShopReviewReportRepository shopReviewReportRepository;

    public ShopReviewReportFixture(
        ShopReviewReportRepository shopReviewReportRepository) {
        this.shopReviewReportRepository = shopReviewReportRepository;
    }

    public ShopReviewReport 리뷰_신고(Student student, ShopReview shopReview, ReportStatus reportStatus) {
        return shopReviewReportRepository.save(
            ShopReviewReport.builder()
                .review(shopReview)
                .title("기타")
                .content("부적절한 리뷰입니다.")
                .reportStatus(reportStatus)
                .userId(student)
                .build()
        );
    }
}
