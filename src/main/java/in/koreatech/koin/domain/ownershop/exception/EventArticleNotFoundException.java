package in.koreatech.koin.domain.ownershop.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class EventArticleNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 이벤트 입니다..";
    private final String detail;

    public EventArticleNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public EventArticleNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static EventArticleNotFoundException withDetail(String detail) {
        return new EventArticleNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
