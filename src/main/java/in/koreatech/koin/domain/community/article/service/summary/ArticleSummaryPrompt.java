package in.koreatech.koin.domain.community.article.service.summary;

public record ArticleSummaryPrompt(
    String systemMessage,
    String userMessage,
    String sourceText,
    int maxItems
) {
    public static final int MAX_TEXT_LENGTH = 100;
}
