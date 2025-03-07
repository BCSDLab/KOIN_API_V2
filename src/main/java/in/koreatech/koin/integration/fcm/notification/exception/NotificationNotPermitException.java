package in.koreatech.koin.integration.fcm.notification.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;

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
