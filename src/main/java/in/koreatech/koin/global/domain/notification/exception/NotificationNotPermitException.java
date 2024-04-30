package in.koreatech.koin.global.domain.notification.exception;

import in.koreatech.koin.global.exception.KoinIllegalStateException;

public class NotificationNotPermitException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "푸쉬알림을 동의하지 않았습니다.";

    public NotificationNotPermitException(String message) {
        super(message);
    }

    public NotificationNotPermitException(String message, String detail) {
        super(message, detail);
    }

    public static NotificationNotPermitException withDetail(String detail) {
        return new NotificationNotPermitException(DEFAULT_MESSAGE, detail);
    }
}
