package in.koreatech.koin.domain.activity.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ActivityException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "복덕방이 존재하지 않습니다.";

    public ActivityException(String message) {
        super(message);
    }

    public static ActivityException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ActivityException(message);
    }
}
