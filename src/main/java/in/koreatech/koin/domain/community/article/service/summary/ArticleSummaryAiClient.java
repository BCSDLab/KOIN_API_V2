package in.koreatech.koin.domain.community.article.service.summary;

public interface ArticleSummaryAiClient {

    ArticleSummaryResult summarize(ArticleSummaryPrompt prompt);
}
