package in.koreatech.koin.domain.community.article.service.summary;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleAiSummaryWorker {

    private final ArticleAiSummaryService articleAiSummaryService;
    private final ArticleSummarySourceReader sourceReader;
    private final ArticleSummaryPromptBuilder promptBuilder;
    private final ArticleSummaryAiClient articleSummaryAiClient;
    private final ArticleSummaryValidator validator;

    @Async("articleSummaryTaskExecutor")
    public void process(Integer summaryId, String workerId) {
        try {
            articleAiSummaryService.getGenerationSeed(summaryId, workerId)
                .ifPresent(seed -> generate(summaryId, workerId, seed));
        } catch (Exception e) {
            log.error("게시글 AI 요약 생성 중 오류가 발생했습니다. summaryId: {}", summaryId, e);
            articleAiSummaryService.completeFailure(summaryId, workerId, e.getMessage());
        }
    }

    private void generate(Integer summaryId, String workerId, ArticleSummarySourceSeed seed) {
        ArticleSummarySource source = sourceReader.read(seed);
        if (!StringUtils.hasText(source.mergedText())) {
            articleAiSummaryService.skip(summaryId, workerId, "요약에 사용할 본문 또는 첨부 문서 내용이 없습니다.");
            return;
        }

        ArticleSummaryPrompt prompt = promptBuilder.build(source);
        ArticleSummaryResult result = articleSummaryAiClient.summarize(prompt);
        if (result == null || result.items() == null || result.items().isEmpty()) {
            articleAiSummaryService.skip(summaryId, workerId, "요약할 핵심 정보가 없습니다.");
            return;
        }
        List<String> summaryLines = validator.validate(result, prompt.sourceText());
        articleAiSummaryService.completeSuccess(summaryId, workerId, source, summaryLines);
    }
}
