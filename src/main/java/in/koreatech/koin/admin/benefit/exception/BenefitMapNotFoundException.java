package in.koreatech.koin.admin.benefit.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BenefitMapNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 혜택 카테고리에 존재하지 않는 입니다.";

    public BenefitMapNotFoundException(String message) {
        super(message);
    }

    public BenefitMapNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BenefitMapNotFoundException withDetail(String detail) {
        return new BenefitMapNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
