package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ShopCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 카테고리입니다.";

    public ShopCategoryNotFoundException(String message) {
        super(message);
    }

    public ShopCategoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ShopCategoryNotFoundException withDetail(String detail) {
        return new ShopCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
