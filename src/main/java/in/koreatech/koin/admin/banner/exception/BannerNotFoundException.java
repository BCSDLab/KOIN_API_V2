package in.koreatech.koin.admin.banner.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class BannerNotFoundException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "해당 ID의 배너가 존재하지 않습니다.";

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
