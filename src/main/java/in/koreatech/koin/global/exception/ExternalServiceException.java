package in.koreatech.koin.global.exception;

public abstract class ExternalServiceException extends RuntimeException {

    protected ExternalServiceException(String message) {
        super(message);
    }

    public abstract String getDetail();
}
