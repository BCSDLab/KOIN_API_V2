package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 카테고리입니다.";
    private final String detail;

    public ShopCategoryNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ShopCategoryNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ShopCategoryNotFoundException withDetail(String detail) {
        return new ShopCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
