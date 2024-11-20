package in.koreatech.koin.domain.bus.batch.exception;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class MalformedApiUriException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "올바르지 않은 URI 형식입니다.";

    public MalformedApiUriException(String message) {
        super(message);
    }

    public MalformedApiUriException(String message, String detail) {
        super(message, detail);
    }

    public static MalformedApiUriException withDetail(String detail) {
        return new MalformedApiUriException(DEFAULT_MESSAGE, detail);
    }
}