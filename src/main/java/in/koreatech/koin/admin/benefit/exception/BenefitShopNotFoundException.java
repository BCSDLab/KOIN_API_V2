package in.koreatech.koin.admin.benefit.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class BenefitShopNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 혜택 카테고리에 존재하지 않는 상점입니다.";

    public BenefitShopNotFoundException(String message) {
        super(message);
    }

    public BenefitShopNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BenefitShopNotFoundException withDetail(String detail) {
        return new BenefitShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
