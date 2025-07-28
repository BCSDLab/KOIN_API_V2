package in.koreatech.koin.admin.shop.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class ShopCategoryNotEmptyException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "카테고리에 상점이 남아있어 삭제할 수 없습니다";

    public ShopCategoryNotEmptyException(String message) {
        super(message);
    }

    public ShopCategoryNotEmptyException(String message, String detail) {
        super(message, detail);
    }

    public static ShopCategoryNotEmptyException withDetail(String detail) {
        return new ShopCategoryNotEmptyException(DEFAULT_MESSAGE, detail);
    }
}
