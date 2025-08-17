package in.koreatech.koin.admin.shop.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ShopParentCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상위 카테고리입니다.";

    public ShopParentCategoryNotFoundException(String message) {
        super(message);
    }

    public ShopParentCategoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ShopParentCategoryNotFoundException withDetail(String detail) {
        return new ShopParentCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
