package in.koreatech.koin._common.exception.custom;

public abstract class DataNotFoundException extends KoinException {

    protected DataNotFoundException(String message) {
        super(message);
    }

    protected DataNotFoundException(String message, String detail) {
        super(message, detail);
    }
}
