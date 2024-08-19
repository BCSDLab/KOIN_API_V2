package in.koreatech.koin.domain.community.keywords.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ArticleKeywordUserMapNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 키워드 입니다.";

    public ArticleKeywordUserMapNotFoundException(String message) {
        super(message);
    }

    public ArticleKeywordUserMapNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ArticleKeywordUserMapNotFoundException withDetail(String detail) {
        return new ArticleKeywordUserMapNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
