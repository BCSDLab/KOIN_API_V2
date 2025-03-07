package in.koreatech.koin._common.exception.custom;

public class KoinIllegalStateException extends KoinException {

    private static final String DEFAULT_MESSAGE = "서버에 문제가 생겼습니다.";

    public KoinIllegalStateException(String message) {
        super(message);
    }

    public KoinIllegalStateException(String message, String detail) {
        super(message, detail);
    }

    public static KoinIllegalStateException withDetail(String detail) {
        return new KoinIllegalStateException(DEFAULT_MESSAGE, detail);
    }
}
