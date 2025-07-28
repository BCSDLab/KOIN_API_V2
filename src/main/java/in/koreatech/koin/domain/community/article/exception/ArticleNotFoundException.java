package in.koreatech.koin.domain.community.article.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ArticleNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "게시글이 존재하지 않습니다.";

    public ArticleNotFoundException(String message) {
        super(message);
    }

    public ArticleNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ArticleNotFoundException withDetail(String detail) {
        return new ArticleNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
