package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Clock;
import java.time.Duration;

import org.springframework.http.HttpHeaders;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;

final class UpstageChatTokenRateLimiter {

    private static final String TOKEN_LIMIT_HEADER = "X-Upstage-Ratelimit-Limit-Tokens";
    private static final String TOKEN_REMAINING_HEADER = "X-Upstage-Ratelimit-Remaining-Tokens";
    private static final String TOKEN_RESET_HEADER = "X-Upstage-Ratelimit-Reset-Tokens";
    private static final int TOKEN_ESTIMATION_NUMERATOR = 5;
    private static final int TOKEN_ESTIMATION_DENOMINATOR = 4;
    private static final int TOKEN_ESTIMATION_OVERHEAD = 5_000;
    private static final long DEFAULT_RESET_MILLIS = 60_000;
    private static final long RESET_BUFFER_MILLIS = 250;
    private static final long MAX_WAIT_MILLIS = 61_000;

    private final Clock clock;
    private final Sleeper sleeper;

    private long tokenLimit = -1;
    private long remainingTokens = -1;
    private long resetAtMillis = -1;

    UpstageChatTokenRateLimiter() {
        this(Clock.systemUTC(), Thread::sleep);
    }

    UpstageChatTokenRateLimiter(Clock clock, Sleeper sleeper) {
        this.clock = clock;
        this.sleeper = sleeper;
    }

    synchronized void await(ArticleSummaryPrompt prompt, int maxOutputTokens) {
        refreshWindow();
        if (remainingTokens < 0 || tokenLimit < 0) {
            return;
        }
        long estimatedTokens = estimateTokens(prompt, maxOutputTokens);
        if (estimatedTokens > tokenLimit) {
            throw new ArticleSummaryExternalApiException(
                "Upstage 요약 API 단일 요청 예상 토큰이 계정 rate limit을 초과합니다.",
                false,
                null
            );
        }
        if (remainingTokens >= estimatedTokens) {
            remainingTokens -= estimatedTokens;
            return;
        }
        if (resetAtMillis < 0) {
            resetAtMillis = clock.millis() + DEFAULT_RESET_MILLIS;
        }

        long remainingResetMillis = resetAtMillis - clock.millis() + RESET_BUFFER_MILLIS;
        if (remainingResetMillis <= 0) {
            resetWindow();
            remainingTokens -= estimatedTokens;
            return;
        }
        long waitMillis = Math.min(remainingResetMillis, MAX_WAIT_MILLIS);
        try {
            sleeper.sleep(waitMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArticleSummaryExternalApiException(
                "Upstage 요약 API 토큰 한도 대기 중 인터럽트가 발생했습니다.",
                true,
                null,
                e
            );
        }
        refreshWindow();
        if (resetAtMillis >= 0) {
            Duration retryAfter = Duration.ofMillis(
                Math.max(resetAtMillis - clock.millis() + RESET_BUFFER_MILLIS, RESET_BUFFER_MILLIS)
            );
            throw new ArticleSummaryExternalApiException(
                "Upstage 요약 API rate limit 토큰 한도 초기화를 기다립니다.",
                true,
                retryAfter
            );
        }
        remainingTokens -= estimatedTokens;
    }

    synchronized void update(HttpHeaders headers) {
        tokenLimit = parseHeader(headers, TOKEN_LIMIT_HEADER, tokenLimit);
        remainingTokens = parseHeader(headers, TOKEN_REMAINING_HEADER, remainingTokens);
        long resetEpochSeconds = parseHeader(headers, TOKEN_RESET_HEADER, -1);
        if (resetEpochSeconds >= 0) {
            resetAtMillis = resetEpochSeconds * 1_000;
        }
    }

    long estimateTokens(ArticleSummaryPrompt prompt, int maxOutputTokens) {
        long characterCount = (long)prompt.systemMessage().length() + prompt.userMessage().length();
        long estimatedPromptTokens = characterCount * TOKEN_ESTIMATION_NUMERATOR / TOKEN_ESTIMATION_DENOMINATOR;
        return estimatedPromptTokens + TOKEN_ESTIMATION_OVERHEAD + maxOutputTokens;
    }

    private void refreshWindow() {
        if (resetAtMillis >= 0 && clock.millis() >= resetAtMillis) {
            resetWindow();
        }
    }

    private void resetWindow() {
        remainingTokens = tokenLimit;
        resetAtMillis = -1;
    }

    private long parseHeader(HttpHeaders headers, String name, long fallback) {
        String value = headers.getFirst(name);
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @FunctionalInterface
    interface Sleeper {

        void sleep(long millis) throws InterruptedException;
    }
}
