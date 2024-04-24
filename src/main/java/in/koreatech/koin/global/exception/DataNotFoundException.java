package in.koreatech.koin.global.exception;

public abstract class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String message) {
        super(message);
    }

    public abstract String getDefaultMessage();
}
