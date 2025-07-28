package in.koreatech.koin.admin.shop.exception;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class ShopCategoryDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "카테고리명이 이미 존재합니다";

    protected ShopCategoryDuplicationException(String message) {
        super(message);
    }

    protected ShopCategoryDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static ShopCategoryDuplicationException withDetail(String name) {
        return new ShopCategoryDuplicationException(DEFAULT_MESSAGE, "name: " + name);
    }
}
