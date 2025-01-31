package in.koreatech.koin.domain.community.article.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class NotALostItemArticleException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 게시글은 분실물 게시글이 아닙니다.";

    public NotALostItemArticleException(String message) {
        super(message);
    }

    public NotALostItemArticleException(String message, String detail) {
        super(message, detail);
    }

    public static ArticleNotFoundException withDetail(String detail) {
        return new ArticleNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
