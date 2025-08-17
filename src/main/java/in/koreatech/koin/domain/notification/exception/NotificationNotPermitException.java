package in.koreatech.koin.domain.notification.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;

public class NotificationNotPermitException extends KoinIllegalArgumentException {

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
