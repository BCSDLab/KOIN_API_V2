package in.koreatech.koin.domain.community.exception;

public class ArticleNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "게시글이 존재하지 않습니다.";

    public ArticleNotFoundException(String message) {
        super(message);
    }

    public static ArticleNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new ArticleNotFoundException(message);
    }
}
