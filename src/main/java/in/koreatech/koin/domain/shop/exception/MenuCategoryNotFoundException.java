package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class MenuCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴카테고리입니다.";

    public MenuCategoryNotFoundException(String message) {
        super(message);
    }

    public MenuCategoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static MenuCategoryNotFoundException withDetail(String detail) {
        return new MenuCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
