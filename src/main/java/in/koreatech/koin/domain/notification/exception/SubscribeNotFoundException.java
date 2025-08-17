package in.koreatech.koin.domain.notification.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class SubscribeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림타입입니다.";

    public SubscribeNotFoundException(String message) {
        super(message);
    }

    public SubscribeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static SubscribeNotFoundException withDetail(String detail) {
        return new SubscribeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
