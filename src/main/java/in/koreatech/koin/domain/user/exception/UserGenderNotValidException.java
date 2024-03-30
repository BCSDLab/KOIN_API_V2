package in.koreatech.koin.domain.user.exception;

public class UserGenderNotValidException extends IllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "잘못된 성별 인덱스입니다.";

    public UserGenderNotValidException(String message) {
        super(message);
    }

    public static UserGenderNotValidException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new UserGenderNotValidException(message);
    }
}
