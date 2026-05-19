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
    private final ArticleAiSummaryProperties properties;

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
        RefinementResult refinementResult = refineIfTooManyItems(result, prompt);
        result = refinementResult.result();
        if (result == null || result.items() == null || result.items().isEmpty()) {
            if (refinementResult.attempted()) {
                throw new ArticleSummaryValidationException("재선별 결과가 비어 있습니다.");
            }
            articleAiSummaryService.skip(summaryId, workerId, "요약할 핵심 정보가 없습니다.");
            return;
        }
        if (validator.hasTooManyItems(result)) {
            articleAiSummaryService.skip(summaryId, workerId, "요약 항목 재선별 후에도 최대 3개를 초과했습니다.");
            return;
        }
        List<String> summaryLines = validateOrRefine(result, prompt, refinementResult.attempted());
        articleAiSummaryService.completeSuccess(summaryId, workerId, source, summaryLines);
    }

    private List<String> validateOrRefine(
        ArticleSummaryResult result,
        ArticleSummaryPrompt originalPrompt,
        boolean alreadyRefined
    ) {
        try {
            return validator.validate(result, originalPrompt.sourceText());
        } catch (ArticleSummaryValidationException e) {
            if (alreadyRefined || properties.getBoundedMaxRefinementRetryCount() == 0) {
                throw e;
            }
            log.info("게시글 AI 요약 최종 검증에 실패해 한 번 더 재작성합니다. reason: {}", e.getMessage());
            ArticleSummaryResult refinedResult = articleSummaryAiClient.summarize(
                promptBuilder.buildRefinement(originalPrompt, result)
            );
            if (refinedResult == null || refinedResult.items() == null || refinedResult.items().isEmpty()) {
                throw new ArticleSummaryValidationException("재작성 결과가 비어 있습니다.");
            }
            if (validator.hasTooManyItems(refinedResult)) {
                throw new ArticleSummaryValidationException("재작성 후에도 요약 항목은 최대 3개까지 허용됩니다.");
            }
            return validator.validate(refinedResult, originalPrompt.sourceText());
        }
    }

    private RefinementResult refineIfTooManyItems(ArticleSummaryResult result, ArticleSummaryPrompt originalPrompt) {
        ArticleSummaryResult refinedResult = result;
        int maxAttemptCount = properties.getBoundedMaxRefinementRetryCount();
        boolean attempted = false;
        for (int attempt = 1; attempt <= maxAttemptCount; attempt++) {
            if (!validator.hasTooManyItems(refinedResult)) {
                return new RefinementResult(refinedResult, attempted);
            }
            attempted = true;
            log.info(
                "게시글 AI 요약 항목이 {}개라 핵심 {}개 이하로 재선별합니다. attempt: {}/{}",
                refinedResult.items().size(),
                validator.maxItems(),
                attempt,
                maxAttemptCount
            );
            refinedResult = articleSummaryAiClient.summarize(
                promptBuilder.buildRefinement(originalPrompt, refinedResult)
            );
        }
        return new RefinementResult(refinedResult, attempted);
    }

    private record RefinementResult(
        ArticleSummaryResult result,
        boolean attempted
    ) {
    }
}
