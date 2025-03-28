package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class OneReviewPerDayException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "한 상점에 하루에 한번만 리뷰를 남길 수 있습니다.";

    public OneReviewPerDayException(String message) {
        super(message);
    }

    public OneReviewPerDayException(String message, String detail) {
        super(message, detail);
    }

    public static OneReviewPerDayException withDetail(String detail) {
        return new OneReviewPerDayException(DEFAULT_MESSAGE, detail);
    }
}
