package in.koreatech.koin.domain.community.keyword.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ArticleKeywordNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 키워드 단어 입니다.";

    public ArticleKeywordNotFoundException(String message) {
        super(message);
    }

    public ArticleKeywordNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ArticleKeywordNotFoundException withDetail(String detail) {
        return new ArticleKeywordNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
