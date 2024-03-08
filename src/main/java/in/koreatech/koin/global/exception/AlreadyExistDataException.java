package in.koreatech.koin.global.exception;

public class AlreadyExistDataException extends RuntimeException{
    public AlreadyExistDataException(String message) {
        super(message);
    }
}
