package in.koreatech.koin.domain.community.article.service.summary;

public record ArticleSummaryPrompt(
    String systemMessage,
    String userMessage,
    String sourceText,
    int maxItems
) {
}
