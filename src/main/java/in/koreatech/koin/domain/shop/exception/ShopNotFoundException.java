package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ShopNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점입니다.";

    public ShopNotFoundException(String message) {
        super(message);
    }

    public ShopNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ShopNotFoundException withDetail(String detail) {
        return new ShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
