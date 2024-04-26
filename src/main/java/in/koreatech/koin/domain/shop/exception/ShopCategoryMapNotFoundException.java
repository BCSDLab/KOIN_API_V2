package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopCategoryMapNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 카테고리맵입니다.";
    private final String detail;

    public ShopCategoryMapNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ShopCategoryMapNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ShopCategoryMapNotFoundException withDetail(String detail) {
        return new ShopCategoryMapNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
