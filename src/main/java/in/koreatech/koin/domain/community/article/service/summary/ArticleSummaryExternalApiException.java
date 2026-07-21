package in.koreatech.koin.domain.community.article.service.summary;

import java.time.Duration;

import lombok.Getter;

@Getter
public class ArticleSummaryExternalApiException extends RuntimeException {

    private final boolean retryable;
    private final Duration retryAfter;

    public ArticleSummaryExternalApiException(
        String message,
        boolean retryable,
        Duration retryAfter
    ) {
        super(message);
        this.retryable = retryable;
        this.retryAfter = retryAfter;
    }

    public ArticleSummaryExternalApiException(
        String message,
        boolean retryable,
        Duration retryAfter,
        Throwable cause
    ) {
        super(message, cause);
        this.retryable = retryable;
        this.retryAfter = retryAfter;
    }
}
