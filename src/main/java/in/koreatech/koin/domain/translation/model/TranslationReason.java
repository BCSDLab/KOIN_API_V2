package in.koreatech.koin.domain.translation.model;

public enum TranslationReason {
    UNSUPPORTED_CONTENT(false), SOURCE_NOT_READY(true), INPUT_TOO_LONG(false),
    TEMPORARY_ERROR(true), MISSING_TRANSLATION(true), INVALID_TRANSLATION(true), RATE_LIMITED(true),
    TIMEOUT(true), CONFIGURATION_ERROR(false), UPSTREAM_REJECTED(false);

    private final boolean retryable;

    TranslationReason(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
