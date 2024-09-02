package in.koreatech.koin.domain.shop.model;

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
