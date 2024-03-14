package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopImageNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점 이미지입니다.";

    public ShopImageNotFoundException(String message) {
        super(message);
    }

    public static ShopImageNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ShopImageNotFoundException(message);
    }
}
