package in.koreatech.koin.domain.community.article.service.summary;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

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
        } catch (ArticleSummaryExternalApiException e) {
            if (e.isRetryable()) {
                articleAiSummaryService.completeFailure(summaryId, workerId, e.getMessage(), e.getRetryAfter());
                return;
            }
            articleAiSummaryService.deferFailureUntilDailyRetry(summaryId, workerId, e.getMessage());
        } catch (Exception e) {
            log.error(
                "게시글 AI 요약 워커에서 예기치 못한 예외가 발생했습니다. summaryId: {}, workerId: {}, exception: {}",
                summaryId,
                workerId,
                e.getClass().getSimpleName(),
                e
            );
            articleAiSummaryService.completeFailure(summaryId, workerId, exceptionSummary(e));
        }
    }

    private void generate(Integer summaryId, String workerId, ArticleSummarySourceSeed seed) {
        ArticleSummarySource source = sourceReader.read(seed);
        if (!StringUtils.hasText(source.mergedText())) {
            if (source.hasTemporaryAttachmentFailure()) {
                throw new ArticleSummaryExternalApiException("첨부 문서 파싱이 일시 실패해 요약 생성을 보류합니다.", true, null);
            }
            articleAiSummaryService.skip(summaryId, workerId, "요약에 사용할 본문 또는 첨부 문서 내용이 없습니다.");
            return;
        }
        if (shouldWaitForTemporaryAttachment(source)) {
            throw new ArticleSummaryExternalApiException("첨부 중심 게시글의 문서 파싱이 일시 실패해 요약 생성을 보류합니다.", true, null);
        }
        if (isAttachmentOnlyContent(source) && source.attachmentTexts().isEmpty()) {
            articleAiSummaryService.skip(summaryId, workerId, "첨부 중심 게시글이지만 요약에 사용할 첨부 문서 내용이 없습니다.");
            return;
        }

        List<String> summaryLines = generateSummary(promptBuilder.build(source));
        if (summaryLines.isEmpty()) {
            articleAiSummaryService.skip(summaryId, workerId, "요약할 핵심 정보가 없습니다.");
            return;
        }
        articleAiSummaryService.completeSuccess(summaryId, workerId, source, summaryLines);
    }

    private List<String> generateSummary(ArticleSummaryPrompt prompt) {
        List<String> validLines = List.of();
        try {
            ArticleSummaryResult result = articleSummaryAiClient.summarize(prompt);
            if (result == null || result.items() == null || result.items().isEmpty()) {
                return List.of();
            }
            result = refineIfTooManyItems(result, prompt);
            if (result == null || result.items() == null || result.items().isEmpty()) {
                throw new ArticleSummaryValidationException("재선별 결과가 비어 있습니다.");
            }
            ArticleSummaryValidator.ValidationResult validation = validator.validateFilteringInvalidItems(
                result, prompt.sourceText(), prompt.maxTextLength()
            );
            validLines = validation.validLines();
            if (!validation.hasFailures() || properties.getBoundedMaxRefinementRetryCount() == 0) {
                return validLinesOrThrow(validation);
            }

            log.info("게시글 AI 요약 최종 검증에 실패해 한 번 더 재작성합니다. reason: {}", validation.firstFailureReason());
            ArticleSummaryResult corrected = articleSummaryAiClient.summarize(
                promptBuilder.buildValidationCorrection(prompt, result, validation.firstFailureReason())
            );
            return validLinesOrThrow(validateRefinedResult(corrected, prompt));
        } catch (ArticleSummaryExternalApiException | ArticleSummaryValidationException e) {
            return generateLegacyFallback(prompt, validLines, e);
        }
    }

    private List<String> generateLegacyFallback(
        ArticleSummaryPrompt originalPrompt,
        List<String> validLines,
        RuntimeException failure
    ) {
        try {
            // 인증 오류와 서버가 지정한 대기 시간은 분량을 바꿔도 즉시 재호출하지 않는다.
            if (failure instanceof ArticleSummaryExternalApiException external
                && (!external.isRetryable() || (external.getRetryAfter() != null
                && external.getRetryAfter().compareTo(Duration.ZERO) > 0))) {
                throw failure;
            }
            log.warn("짧은 요약 생성에 실패해 이전 200자 기준으로 한 번 생성합니다. reason: {}", failure.getMessage());
            ArticleSummaryPrompt fallbackPrompt = promptBuilder.buildLegacyFallback(originalPrompt);
            ArticleSummaryResult fallback = articleSummaryAiClient.summarize(fallbackPrompt);
            return validLinesOrThrow(validateRefinedResult(fallback, fallbackPrompt));
        } catch (ArticleSummaryExternalApiException | ArticleSummaryValidationException e) {
            if (!validLines.isEmpty()) {
                log.warn("요약 재생성에 실패해 이미 검증된 {}개 항목을 저장합니다. reason: {}", validLines.size(), e.getMessage());
                return validLines;
            }
            throw e;
        }
    }

    private ArticleSummaryValidator.ValidationResult validateRefinedResult(
        ArticleSummaryResult refinedResult,
        ArticleSummaryPrompt originalPrompt
    ) {
        if (refinedResult == null || refinedResult.items() == null || refinedResult.items().isEmpty()) {
            return new ArticleSummaryValidator.ValidationResult(List.of(), List.of("재작성 결과가 비어 있습니다."));
        }
        if (validator.hasTooManyItems(refinedResult)) {
            return new ArticleSummaryValidator.ValidationResult(
                List.of(),
                List.of("재작성 후에도 요약 항목은 최대 3개까지 허용됩니다.")
            );
        }
        return validator.validateFilteringInvalidItems(
            refinedResult, originalPrompt.sourceText(), originalPrompt.maxTextLength()
        );
    }

    private List<String> validLinesOrThrow(ArticleSummaryValidator.ValidationResult validationResult) {
        if (validationResult.hasValidLines()) {
            logInvalidSummaryItems(validationResult);
            return validationResult.validLines();
        }
        throw new ArticleSummaryValidationException(validationResult.firstFailureReason());
    }

    private void logInvalidSummaryItems(ArticleSummaryValidator.ValidationResult validationResult) {
        if (!validationResult.hasFailures()) {
            return;
        }
        log.warn(
            "게시글 AI 요약 일부 항목이 검증 실패로 제외되었습니다. reasons: {}",
            String.join(" | ", validationResult.failureReasons())
        );
    }

    private ArticleSummaryResult refineIfTooManyItems(ArticleSummaryResult result, ArticleSummaryPrompt originalPrompt) {
        ArticleSummaryResult refinedResult = result;
        int maxAttemptCount = properties.getBoundedMaxRefinementRetryCount();
        for (int attempt = 1; attempt <= maxAttemptCount; attempt++) {
            if (!validator.hasTooManyItems(refinedResult)) {
                return refinedResult;
            }
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
        return refinedResult;
    }

    private boolean shouldWaitForTemporaryAttachment(ArticleSummarySource source) {
        if (!source.hasTemporaryAttachmentFailure() || !source.attachmentTexts().isEmpty()) {
            return false;
        }
        return isAttachmentOnlyContent(source);
    }

    private boolean isAttachmentOnlyContent(ArticleSummarySource source) {
        String normalizedContent = normalize(source.contentText());
        if (!StringUtils.hasText(normalizedContent)) {
            return true;
        }
        return normalizedContent.length() <= 120 && (
            normalizedContent.contains("첨부")
                || normalizedContent.contains("붙임")
                || normalizedContent.contains("파일")
                || normalizedContent.contains("문서")
                || normalizedContent.contains("이미지")
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private String exceptionSummary(Exception e) {
        if (!StringUtils.hasText(e.getMessage())) {
            return e.getClass().getSimpleName();
        }
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

}
