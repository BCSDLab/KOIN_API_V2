package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;

public record ShopReviewReportCache(
        String title,
        String content,
        ReportStatus reportStatus,
        Integer userId
) {
    public static ShopReviewReportCache from(ShopReviewReport shopReviewReport)  {
        return new ShopReviewReportCache(
                shopReviewReport.getTitle(),
                shopReviewReport.getContent(),
                shopReviewReport.getReportStatus(),
                shopReviewReport.getUserId().getId()
        );
    }
}
