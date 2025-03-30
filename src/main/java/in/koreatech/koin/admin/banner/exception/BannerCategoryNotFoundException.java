package in.koreatech.koin.admin.banner.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class BannerCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "배너 카테고리가 존재하지 않습니다.";

    public BannerCategoryNotFoundException(String message) {
        super(message);
    }

    public BannerCategoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BannerCategoryNotFoundException withDetail(String detail) {
        return new BannerCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
