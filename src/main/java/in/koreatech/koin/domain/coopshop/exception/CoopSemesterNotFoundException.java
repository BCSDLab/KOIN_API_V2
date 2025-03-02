package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class CoopSemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 학기가 존재하지 않습니다.";

    public CoopSemesterNotFoundException(String message) {
        super(message);
    }

    public CoopSemesterNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CoopShopNotFoundException withDetail(String detail) {
        return new CoopShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
