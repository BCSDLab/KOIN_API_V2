package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;

class UpstageChatTokenRateLimiterTest {

    private static final int MAX_OUTPUT_TOKENS = 2_048;

    @Test
    void 남은_토큰이_부족하면_한도_초기화_시점으로_재시도를_예약한다() {
        Clock clock = fixedClock();
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter(clock);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstage-Ratelimit-Limit-Tokens", "150000");
        headers.add("X-Upstage-Ratelimit-Remaining-Tokens", "10000");
        headers.add("X-Upstage-Ratelimit-Reset-Tokens", "1001");
        rateLimiter.update(headers);

        assertThatThrownBy(() -> rateLimiter.await(prompt("가".repeat(20_000)), MAX_OUTPUT_TOKENS))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .satisfies(throwable -> assertThat(((ArticleSummaryExternalApiException)throwable).getRetryAfter())
                .isEqualTo(Duration.ofMillis(1_250)));
    }

    @Test
    void 초기화_헤더가_없으면_기본_1분_창을_사용한다() {
        Clock clock = fixedClock();
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter(clock);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstage-Ratelimit-Limit-Tokens", "150000");
        headers.add("X-Upstage-Ratelimit-Remaining-Tokens", "10000");
        rateLimiter.update(headers);

        assertThatThrownBy(() -> rateLimiter.await(prompt("가".repeat(20_000)), MAX_OUTPUT_TOKENS))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .satisfies(throwable -> assertThat(((ArticleSummaryExternalApiException)throwable).getRetryAfter())
                .isEqualTo(Duration.ofMillis(60_250)));
    }

    @Test
    void 초기화까지_오래_남아도_스레드를_점유하지_않고_재시도를_예약한다() {
        Clock clock = fixedClock();
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter(clock);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstage-Ratelimit-Limit-Tokens", "150000");
        headers.add("X-Upstage-Ratelimit-Remaining-Tokens", "10000");
        headers.add("X-Upstage-Ratelimit-Reset-Tokens", "1120");
        rateLimiter.update(headers);

        assertThatThrownBy(() -> rateLimiter.await(prompt("가".repeat(20_000)), MAX_OUTPUT_TOKENS))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .satisfies(throwable -> assertThat(((ArticleSummaryExternalApiException)throwable).getRetryAfter())
                .isEqualTo(Duration.ofMillis(120_250)));
    }

    @Test
    void 요청할_토큰은_응답을_기다리기_전에_남은_한도에서_예약한다() {
        Clock clock = fixedClock();
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter(clock);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstage-Ratelimit-Limit-Tokens", "150000");
        headers.add("X-Upstage-Ratelimit-Remaining-Tokens", "150000");
        headers.add("X-Upstage-Ratelimit-Reset-Tokens", "1120");
        rateLimiter.update(headers);

        rateLimiter.await(prompt("가".repeat(40_000)), MAX_OUTPUT_TOKENS);

        assertThatThrownBy(() -> rateLimiter.await(prompt("가".repeat(80_000)), MAX_OUTPUT_TOKENS))
            .isInstanceOf(ArticleSummaryExternalApiException.class);
    }

    @Test
    void 단일_요청_예상량이_계정_한도보다_크면_재시도하지_않는다() {
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Upstage-Ratelimit-Limit-Tokens", "50000");
        headers.add("X-Upstage-Ratelimit-Remaining-Tokens", "50000");
        rateLimiter.update(headers);

        assertThatThrownBy(() -> rateLimiter.await(prompt("가".repeat(80_000)), MAX_OUTPUT_TOKENS))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .satisfies(throwable -> assertThat(((ArticleSummaryExternalApiException)throwable).isRetryable()).isFalse());
    }

    @Test
    void 긴_입력은_문자수보다_넉넉하게_토큰을_예약한다() {
        UpstageChatTokenRateLimiter rateLimiter = new UpstageChatTokenRateLimiter();

        long estimatedTokens = rateLimiter.estimateTokens(prompt("가".repeat(80_000)), MAX_OUTPUT_TOKENS);

        assertThat(estimatedTokens).isGreaterThan(100_000);
    }

    private ArticleSummaryPrompt prompt(String userMessage) {
        return new ArticleSummaryPrompt(
            "system message",
            userMessage,
            userMessage,
            3
        );
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.ofEpochSecond(1_000), ZoneOffset.UTC);
    }
}
