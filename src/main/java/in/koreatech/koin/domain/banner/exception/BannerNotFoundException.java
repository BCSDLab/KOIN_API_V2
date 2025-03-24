package in.koreatech.koin.domain.banner.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BannerNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 배너가 존재하지 않습니다.";

    public BannerNotFoundException(String message) {
        super(message);
    }

    public BannerNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BannerNotFoundException withDetail(String detail) {
        return new BannerNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
