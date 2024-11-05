package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewImage;
import java.util.List;

public record ShopReviewCache(
        String content,
        Integer rating,
        Integer reviewerId,
        List<String> images,
        List<ShopReviewReportCache> reports
) {
    public static ShopReviewCache from(ShopReview shopReview) {
        return new ShopReviewCache(
            shopReview.getContent(),
            shopReview.getRating(),
            shopReview.getReviewer().getId(),
            shopReview.getImages().stream().map(ShopReviewImage::getImageUrls).toList(),
            shopReview.getReports().stream().map(ShopReviewReportCache::from).toList()
        );
    }
}
