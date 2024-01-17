package in.koreatech.koin.domain.shop.exception;

public class MenuNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴입니다.";

    public MenuNotFoundException(String message) {
        super(message);
    }

    public static MenuNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new MenuNotFoundException(message);
    }
}
