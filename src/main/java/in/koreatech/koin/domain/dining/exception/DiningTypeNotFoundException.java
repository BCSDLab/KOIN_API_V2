package in.koreatech.koin.domain.dining.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class DiningTypeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "식사 타입이 존재하지 않습니다.";
    private final String detail;

    public DiningTypeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public DiningTypeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static DiningTypeNotFoundException withDetail(String detail) {
        return new DiningTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
