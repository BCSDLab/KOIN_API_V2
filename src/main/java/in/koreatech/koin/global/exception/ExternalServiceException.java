package in.koreatech.koin.global.exception;

public abstract class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public abstract String getDetail();
}
