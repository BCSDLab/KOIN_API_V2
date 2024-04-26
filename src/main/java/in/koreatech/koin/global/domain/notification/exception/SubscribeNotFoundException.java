package in.koreatech.koin.global.domain.notification.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class SubscribeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 알림타입입니다.";
    private final String detail;

    public SubscribeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public SubscribeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static SubscribeNotFoundException withDetail(String detail) {
        return new SubscribeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
