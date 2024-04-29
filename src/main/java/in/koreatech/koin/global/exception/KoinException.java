package in.koreatech.koin.global.exception;

public abstract class KoinException extends RuntimeException {

    protected KoinException(String message) {
        super(message);
    }

    public abstract String getDetail();
}
