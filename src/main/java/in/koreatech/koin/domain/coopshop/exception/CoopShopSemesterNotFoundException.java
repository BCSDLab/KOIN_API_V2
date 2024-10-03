package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class CoopShopSemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 학기가 존재하지 않습니다.";

    public CoopShopSemesterNotFoundException(String message) {
        super(message);
    }

    public CoopShopSemesterNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CoopShopNotFoundException withDetail(String detail) {
        return new CoopShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
