package in.koreatech.koin.global.domain.notification.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class NotificationSubscribeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림 구독입니다.";

    public NotificationSubscribeNotFoundException(String message) {
        super(message);
    }

    public static NotificationSubscribeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new NotificationSubscribeNotFoundException(message);
    }
}
