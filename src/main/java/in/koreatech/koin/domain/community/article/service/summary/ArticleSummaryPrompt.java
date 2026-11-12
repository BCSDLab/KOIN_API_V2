package in.koreatech.koin.domain.community.article.service.summary;

public record ArticleSummaryPrompt(
    String systemMessage,
    String userMessage,
    String sourceText,
    int maxItems,
    int maxTextLength
) {
    public static final int MAX_TEXT_LENGTH = 100;
    public static final int LEGACY_MAX_TEXT_LENGTH = 200;

    public ArticleSummaryPrompt(String systemMessage, String userMessage, String sourceText, int maxItems) {
        this(systemMessage, userMessage, sourceText, maxItems, MAX_TEXT_LENGTH);
    }
}
