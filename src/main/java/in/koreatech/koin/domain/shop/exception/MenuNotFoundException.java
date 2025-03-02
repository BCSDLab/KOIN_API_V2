package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class MenuNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴입니다.";

    public MenuNotFoundException(String message) {
        super(message);
    }

    public MenuNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MenuNotFoundException withDetail(String detail) {
        return new MenuNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
