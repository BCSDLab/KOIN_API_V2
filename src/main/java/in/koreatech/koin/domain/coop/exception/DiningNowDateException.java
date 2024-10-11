package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class DiningNowDateException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "";

    public DiningNowDateException(String message) {
        super(message);
    }

    public DiningNowDateException(String message, String detail) {
        super(message, detail);
    }

    public static DiningNowDateException withDetail(String detail) {
        return new DiningNowDateException(DEFAULT_MESSAGE, detail);
    }
}
