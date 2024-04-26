package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationCertificationException extends DuplicationException {
    private static final String DEFAULT_MESSAGE = "이미 인증이 완료되었습니다.";
    private final String detail;

    public DuplicationCertificationException(String message) {
        super(message);
        this.detail = null;
    }

    public DuplicationCertificationException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static DuplicationCertificationException withDetail(String detail) {
        return new DuplicationCertificationException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
