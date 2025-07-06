package in.koreatech.koin.domain.order.shop.model.domain;

public record OrderableShopInfoSummary(
    Integer shopId,
    Integer orderableShopId,
    String name,
    String introduction,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Boolean payCard,
    Boolean payBank,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Integer reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip
) {
}
