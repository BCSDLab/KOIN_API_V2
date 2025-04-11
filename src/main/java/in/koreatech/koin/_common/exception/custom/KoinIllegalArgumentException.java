package in.koreatech.koin._common.exception.custom;

public class KoinIllegalArgumentException extends KoinException {

    private static final String DEFAULT_MESSAGE = "잘못된 요청입니다.";

    public KoinIllegalArgumentException(String message) {
        super(message);
    }

    public KoinIllegalArgumentException(String message, String detail) {
        super(message, detail);
    }

    public static KoinIllegalArgumentException withDetail(String detail) {
        return new KoinIllegalArgumentException(DEFAULT_MESSAGE, detail);
    }
}
