package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationCompanyNumberException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 사업자 등록번호입니다.";
    private final String detail;

    public DuplicationCompanyNumberException(String message) {
        super(message);
        this.detail = null;
    }

    public DuplicationCompanyNumberException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static DuplicationCompanyNumberException withDetail(String detail) {
        return new DuplicationCompanyNumberException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
