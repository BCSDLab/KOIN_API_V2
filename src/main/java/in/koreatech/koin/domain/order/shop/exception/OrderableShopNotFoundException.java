package in.koreatech.koin.domain.order.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;

public class OrderableShopNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점입니다.";

    public OrderableShopNotFoundException(String message) {
        super(message);
    }

    public OrderableShopNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static OrderableShopNotFoundException withDetail(String detail) {
        return new OrderableShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
