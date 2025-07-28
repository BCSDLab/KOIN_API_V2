package in.koreatech.koin.domain.community.article.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class BoardNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "게시판이 존재하지 않습니다.";

    public BoardNotFoundException(String message) {
        super(message);
    }

    public BoardNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static BoardNotFoundException withDetail(String detail) {
        return new BoardNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
