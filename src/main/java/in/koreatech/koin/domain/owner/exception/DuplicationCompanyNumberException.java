package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicationCompanyNumberException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 사업자 등록번호입니다.";

    public DuplicationCompanyNumberException(String message) {
        super(message);
    }

    public DuplicationCompanyNumberException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicationCompanyNumberException withDetail(String detail) {
        return new DuplicationCompanyNumberException(DEFAULT_MESSAGE, detail);
    }
}
