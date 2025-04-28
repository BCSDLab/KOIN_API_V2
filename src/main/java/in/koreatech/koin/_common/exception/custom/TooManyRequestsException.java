package in.koreatech.koin._common.exception.custom;

public class TooManyRequestsException extends KoinException {

    public TooManyRequestsException(String message) {
        super(message);
    }

    public TooManyRequestsException(String message, String detail) {
        super(message, detail);
    }
}
