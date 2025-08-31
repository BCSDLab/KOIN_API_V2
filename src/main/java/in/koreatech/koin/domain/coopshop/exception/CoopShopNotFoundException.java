package in.koreatech.koin.domain.coopshop.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class CoopShopNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 생협 매장이 존재하지 않습니다.";

    public CoopShopNotFoundException(String message) {
        super(message);
    }

    public CoopShopNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CoopShopNotFoundException withDetail(String detail) {
        return new CoopShopNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
