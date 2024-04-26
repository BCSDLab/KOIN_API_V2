package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ShopOpenNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 상점 오픈입니다.";
    private final String detail;

    public ShopOpenNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ShopOpenNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ShopOpenNotFoundException withDetail(String detail) {
        return new ShopOpenNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
