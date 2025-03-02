package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ReviewNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "리뷰가 존재하지 않습니다.";

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ReviewNotFoundException withDetail(String detail) {
        return new ReviewNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
