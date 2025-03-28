package in.koreatech.koin.admin.banner.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class BannerMobileFieldPairNotMatchException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "모바일 리다이렉션 링크와 모바일 최소 버전은 쌍으로 존재해야 합니다.";

    public BannerMobileFieldPairNotMatchException(String message) {
        super(message);
    }

    public BannerMobileFieldPairNotMatchException(String message, String detail) {
        super(message, detail);
    }

    public static BannerMobileFieldPairNotMatchException withDetail(String detail) {
        return new BannerMobileFieldPairNotMatchException(DEFAULT_MESSAGE, detail);
    }
}
