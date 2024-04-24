package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.domain.shop.exception.MenuNotFoundException;
import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationCompanyNumberException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 사업자 등록번호입니다.";

    public DuplicationCompanyNumberException(String message) {
        super(message);
    }

    public static MenuNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new MenuNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
