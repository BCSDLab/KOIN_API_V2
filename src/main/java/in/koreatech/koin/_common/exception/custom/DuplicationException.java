package in.koreatech.koin._common.exception.custom;

public abstract class DuplicationException extends KoinException {

    protected DuplicationException(String message) {
        super(message);
    }

    protected DuplicationException(String message, String detail) {
        super(message, detail);
    }
}
