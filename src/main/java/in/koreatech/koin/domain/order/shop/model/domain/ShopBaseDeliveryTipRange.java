package in.koreatech.koin.domain.order.shop.model.domain;

public record ShopBaseDeliveryTipRange(
    Integer fromAmount,
    Integer toAmount,
    Integer fee
) {

}
