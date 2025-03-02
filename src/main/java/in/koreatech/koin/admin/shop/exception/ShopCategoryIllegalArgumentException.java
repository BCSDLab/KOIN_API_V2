package in.koreatech.koin.admin.shop.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class ShopCategoryIllegalArgumentException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "카테고리를 찾을 수 없습니다";

    protected ShopCategoryIllegalArgumentException(String message) {
        super(message);
    }

    protected ShopCategoryIllegalArgumentException(String message, String detail) {
        super(message, detail);
    }

    public static ShopCategoryIllegalArgumentException withDetail(String detail) {
        return new ShopCategoryIllegalArgumentException(DEFAULT_MESSAGE, detail);
    }
}
