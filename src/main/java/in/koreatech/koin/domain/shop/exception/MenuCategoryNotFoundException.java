package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class MenuCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴카테고리입니다.";
    private final String detail;

    public MenuCategoryNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public MenuCategoryNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static MenuCategoryNotFoundException withDetail(String detail) {
        return new MenuCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
