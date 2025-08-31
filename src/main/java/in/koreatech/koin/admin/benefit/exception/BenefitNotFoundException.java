package in.koreatech.koin.admin.benefit.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class BenefitNotFoundException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "해당 ID의 혜택 카테고리가 존재하지 않습니다.";

    public BenefitNotFoundException(String message) {
        super(message);
    }

    public BenefitNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BenefitNotFoundException withDetail(String detail) {
        return new BenefitNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
