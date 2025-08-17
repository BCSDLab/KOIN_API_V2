package in.koreatech.koin.common.event;

import in.koreatech.koin.domain.shop.model.review.ShopReview;

public record ReviewRegisterEvent(
    String shop,
    String content,
    Integer rating
) {

    public static ReviewRegisterEvent from(ShopReview shopReview) {
        return new ReviewRegisterEvent(
            shopReview.getShop().getName(),
            shopReview.getContent(),
            shopReview.getRating()
        );
    }
}
