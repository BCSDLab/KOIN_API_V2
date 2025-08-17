package in.koreatech.koin.admin.benefit.exception;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class BenefitDuplicationException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 존재하는 혜택 카테고리입니다.";

    public BenefitDuplicationException(String message) {
        super(message);
    }

    public BenefitDuplicationException(String message, String detail) {
        super(message, detail);
    }

    public static BenefitDuplicationException withDetail(String detail) {
        return new BenefitDuplicationException(DEFAULT_MESSAGE, detail);
    }
}
