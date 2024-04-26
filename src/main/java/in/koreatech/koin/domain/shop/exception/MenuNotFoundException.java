package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MenuNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴입니다.";
    private final String detail;

    public MenuNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public MenuNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static MenuNotFoundException withDetail(String detail) {
        return new MenuNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
