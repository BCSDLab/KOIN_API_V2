package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 카테고리입니다.";

    public ShopCategoryNotFoundException(String message) {
        super(message);
    }

    public static ShopCategoryNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ShopCategoryNotFoundException(message);
    }
}
