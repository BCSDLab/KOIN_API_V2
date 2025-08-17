package in.koreatech.koin.domain.graduation.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class CatalogNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 대학 요람입니다.";

    protected CatalogNotFoundException(String message) {
        super(message);
    }

    protected CatalogNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static CatalogNotFoundException withDetail(String detail) {
        return new CatalogNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
