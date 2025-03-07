package in.koreatech.koin.admin.benefit.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;

public class BenefitLimitException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "혜택 카테고리의 개수가 제한을 초과하였습니다.";

    public BenefitLimitException(String message) {
        super(message);
    }

    public BenefitLimitException(String message, String detail) {
        super(message, detail);
    }

    public static BenefitLimitException withDetail(String detail) {
        return new BenefitLimitException(DEFAULT_MESSAGE, detail);
    }
}
