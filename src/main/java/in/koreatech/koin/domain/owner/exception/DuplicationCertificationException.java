package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class DuplicationCertificationException extends DuplicationException {
    private static final String DEFAULT_MESSAGE = "이미 인증이 완료되었습니다.";

    public DuplicationCertificationException(String message) {
        super(message);
    }

    public static DuplicationCertificationException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new DuplicationCertificationException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
