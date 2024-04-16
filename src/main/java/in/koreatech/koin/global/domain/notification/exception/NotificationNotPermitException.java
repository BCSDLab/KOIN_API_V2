package in.koreatech.koin.global.domain.notification.exception;

public class NotificationNotPermitException extends IllegalStateException {

    private static final String DEFAULT_MESSAGE = "푸쉬알림을 동의하지 않았습니다.";

    public NotificationNotPermitException(String message) {
        super(message);
    }

    public static NotificationNotPermitException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new NotificationNotPermitException(message);
    }
}
