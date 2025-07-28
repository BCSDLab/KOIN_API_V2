package in.koreatech.koin.domain.shop.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class NotificationMessageNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 상점에 해당하는 알림 메세지를 찾지 못했습니다.";

    public NotificationMessageNotFoundException(String message) {
        super(message);
    }

    public NotificationMessageNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static NotificationMessageNotFoundException withDetail(String detail) {
        return new NotificationMessageNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
