package in.koreatech.koin.domain.community.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ArticleNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "게시글이 존재하지 않습니다.";
    private final String detail;

    public ArticleNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ArticleNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ArticleNotFoundException withDetail(String detail) {
        return new ArticleNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return DEFAULT_MESSAGE;
    }
}
