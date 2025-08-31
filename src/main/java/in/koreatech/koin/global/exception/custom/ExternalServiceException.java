package in.koreatech.koin.global.exception.custom;

public abstract class ExternalServiceException extends KoinException {

    protected ExternalServiceException(String message) {
        super(message);
    }

    protected ExternalServiceException(String message, String detail) {
        super(message, detail);
    }
}
