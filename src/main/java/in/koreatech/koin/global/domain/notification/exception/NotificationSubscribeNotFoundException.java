package in.koreatech.koin.global.domain.notification.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class NotificationSubscribeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림 구독입니다.";
    private final String detail;

    public NotificationSubscribeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public NotificationSubscribeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static NotificationSubscribeNotFoundException withDetail(String detail) {
        return new NotificationSubscribeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
