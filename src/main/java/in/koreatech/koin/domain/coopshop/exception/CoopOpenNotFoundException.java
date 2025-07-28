package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class CoopOpenNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 운영 시간이 존재하지 않습니다.";

    public CoopOpenNotFoundException(String message) {
        super(message);
    }

    public CoopOpenNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CoopOpenNotFoundException withDetail(String detail) {
        return new CoopOpenNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
