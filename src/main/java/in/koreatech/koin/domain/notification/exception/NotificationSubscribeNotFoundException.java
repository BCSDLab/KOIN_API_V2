package in.koreatech.koin.domain.notification.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class NotificationSubscribeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림 구독입니다.";

    public NotificationSubscribeNotFoundException(String message) {
        super(message);
    }

    public NotificationSubscribeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static NotificationSubscribeNotFoundException withDetail(String detail) {
        return new NotificationSubscribeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
