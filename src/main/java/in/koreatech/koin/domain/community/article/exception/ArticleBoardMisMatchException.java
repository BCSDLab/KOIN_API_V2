package in.koreatech.koin.domain.community.article.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class ArticleBoardMisMatchException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "게시판 정보와 게시글 정보가 일치하지 않습니다.";

    public ArticleBoardMisMatchException(String message) {
        super(message);
    }

    public ArticleBoardMisMatchException(String message, String detail) {
        super(message, detail);
    }

    public static ArticleBoardMisMatchException withDetail(String detail) {
        return new ArticleBoardMisMatchException(DEFAULT_MESSAGE, detail);
    }
}
