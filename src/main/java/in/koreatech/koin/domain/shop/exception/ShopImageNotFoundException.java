package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopImageNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점 이미지입니다.";
    private final String detail;

    public ShopImageNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ShopImageNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ShopImageNotFoundException withDetail(String detail) {
        return new ShopImageNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
