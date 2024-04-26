package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점입니다.";
    private final String detail;

    public ShopNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ShopNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ShopNotFoundException withDetail(String detail) {
        return new ShopNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
