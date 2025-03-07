package in.koreatech.koin._common.exception.custom;

public abstract class KoinException extends RuntimeException {

    protected final String detail;

    protected KoinException(String message) {
        super(message);
        this.detail = null;
    }

    protected KoinException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    protected String getFullMessage() {
        return String.format("%s %s", getMessage(), detail);
    }
}
