package in.koreatech.koin.global.exception;

public abstract class DuplicationException extends RuntimeException {

    public DuplicationException(String message) {
        super(message);
    }

    public abstract String getDetail();
}
