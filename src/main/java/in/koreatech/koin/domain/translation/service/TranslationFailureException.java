package in.koreatech.koin.domain.translation.service;

import java.time.Duration;

import in.koreatech.koin.domain.translation.model.TranslationReason;
import lombok.Getter;

@Getter
public class TranslationFailureException extends RuntimeException {
    private final TranslationReason reason;
    private final Duration retryAfter;

    public TranslationFailureException(TranslationReason reason, Duration retryAfter) {
        super(reason.name());
        this.reason = reason;
        this.retryAfter = retryAfter;
    }
}
