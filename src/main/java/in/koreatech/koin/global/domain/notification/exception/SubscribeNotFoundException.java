package in.koreatech.koin.global.domain.notification.exception;

import in.koreatech.koin.global.exception.DuplicationException;

public class SubscribeNotFoundException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림타입입니다.";

    public SubscribeNotFoundException(String message) {
        super(message);
    }

    public static SubscribeNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new SubscribeNotFoundException(message);
    }
}
