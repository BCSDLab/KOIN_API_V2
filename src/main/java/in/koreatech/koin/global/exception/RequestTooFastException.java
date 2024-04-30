package in.koreatech.koin.global.exception;

public class RequestTooFastException extends RuntimeException {

    public RequestTooFastException(String message) {
        super(message);
    }
}
