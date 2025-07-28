package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class MenuIllegalSingleException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "단일 메뉴에 대한 유효성 검사 오류입니다.";

    public MenuIllegalSingleException(String message) {
        super(message);
    }

    public MenuIllegalSingleException(String message, String detail) {
        super(message, detail);
    }

    public static MenuIllegalSingleException withDetail(String detail) {
        return new MenuIllegalSingleException(DEFAULT_MESSAGE, detail);
    }
}
