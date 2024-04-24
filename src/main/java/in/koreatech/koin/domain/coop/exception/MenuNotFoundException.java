package in.koreatech.koin.domain.coop.exception;

import in.koreatech.koin.domain.shop.exception.ShopCategoryNotFoundException;
import in.koreatech.koin.global.exception.DataNotFoundException;

public class MenuNotFoundException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "존재하지 않는 메뉴입니다.";

    public MenuNotFoundException(String message) {
        super(message);
    }

    public static ShopCategoryNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ShopCategoryNotFoundException(message);
    }

    @Override
    public String getDefaultMessage() {
        return DEFAULT_MESSAGE;
    }
}
