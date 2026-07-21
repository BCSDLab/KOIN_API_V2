package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class UpstageRetryAfterResolverTest {

    @Test
    void retry_after_헤더가_없는_429는_1분_뒤_재시도한다() {
        Duration retryAfter = UpstageRetryAfterResolver.resolve(429, null);

        assertThat(retryAfter).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void retry_after_헤더가_있으면_헤더_값을_우선한다() {
        Duration retryAfter = UpstageRetryAfterResolver.resolve(429, "30");

        assertThat(retryAfter).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void retry_after_헤더가_없는_5xx는_기본_백오프를_사용하도록_null을_반환한다() {
        Duration retryAfter = UpstageRetryAfterResolver.resolve(500, null);

        assertThat(retryAfter).isNull();
    }

    @Test
    void 응답_전_커넥션이_끊긴_일시_네트워크_오류는_1분_뒤_재시도한다() {
        RuntimeException exception = new RuntimeException(new PrematureCloseException());

        Duration retryAfter = UpstageRetryAfterResolver.resolveTransientFailure(exception);

        assertThat(retryAfter).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void 알_수_없는_일반_예외는_기본_백오프를_사용하도록_null을_반환한다() {
        Duration retryAfter = UpstageRetryAfterResolver.resolveTransientFailure(new IllegalStateException("unknown"));

        assertThat(retryAfter).isNull();
    }

    private static class PrematureCloseException extends RuntimeException {
    }
}
