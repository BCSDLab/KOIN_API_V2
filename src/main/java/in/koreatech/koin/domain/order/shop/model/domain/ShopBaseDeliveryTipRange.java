package in.koreatech.koin.domain.order.shop.model.domain;

public record ShopBaseDeliveryTipRange(
    Integer fromAmount,
    Integer toAmount,
    Integer fee
) {

    public boolean isInRange(Integer orderAmount) {
        if (orderAmount < fromAmount) {
            return false;
        }
        return toAmount == null || orderAmount < toAmount;
    }
}
