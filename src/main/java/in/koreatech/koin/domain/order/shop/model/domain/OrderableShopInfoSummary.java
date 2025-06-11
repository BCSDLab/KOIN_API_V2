package in.koreatech.koin.domain.order.shop.model.domain;

public record OrderableShopInfoSummary(
    Integer shopId,
    Integer orderableShopId,
    String name,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Integer reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip
) {
}
