package in.koreatech.koin.domain.ownershop.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class EventArticleNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이벤트 입니다.";

    public EventArticleNotFoundException(String message) {
        super(message);
    }

    public EventArticleNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static EventArticleNotFoundException withDetail(String detail) {
        return new EventArticleNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
