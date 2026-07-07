package in.koreatech.koin.admin.article.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class AdminArticleAiSummaryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "해당 게시글 AI 요약 작업을 찾을 수 없습니다.";

    public AdminArticleAiSummaryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AdminArticleAiSummaryNotFoundException withDetail(String detail) {
        return new AdminArticleAiSummaryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
