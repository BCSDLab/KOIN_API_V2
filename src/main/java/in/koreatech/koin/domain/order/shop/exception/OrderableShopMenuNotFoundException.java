package in.koreatech.koin.domain.order.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class OrderableShopMenuNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴입니다.";

    public OrderableShopMenuNotFoundException(String message) {
        super(message);
    }

    public OrderableShopMenuNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static OrderableShopMenuNotFoundException withDetail(String detail) {
        return new OrderableShopMenuNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
