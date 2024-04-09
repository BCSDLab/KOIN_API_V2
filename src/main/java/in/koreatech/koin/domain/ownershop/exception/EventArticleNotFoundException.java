package in.koreatech.koin.domain.ownershop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class EventArticleNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이벤트 입니다..";

    public EventArticleNotFoundException(String message) {
        super(message);
    }

    public static EventArticleNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new EventArticleNotFoundException(message);
    }
}
