package in.koreatech.koin._common.exception.custom;

public class RequestTooFastException extends RuntimeException {

    public RequestTooFastException(String message) {
        super(message);
    }
}
