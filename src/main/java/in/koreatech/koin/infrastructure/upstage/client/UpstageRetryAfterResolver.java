package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.util.StringUtils;

final class UpstageRetryAfterResolver {

    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    private static final Duration DEFAULT_RATE_LIMIT_RETRY_AFTER = Duration.ofMinutes(1);
    private static final Duration DEFAULT_TRANSIENT_NETWORK_RETRY_AFTER = Duration.ofMinutes(1);

    private UpstageRetryAfterResolver() {
    }

    static Duration resolve(int status, String retryAfterHeader) {
        Duration parsedRetryAfter = parse(retryAfterHeader);
        if (isPositive(parsedRetryAfter)) {
            return parsedRetryAfter;
        }
        if (status == HTTP_STATUS_TOO_MANY_REQUESTS) {
            return DEFAULT_RATE_LIMIT_RETRY_AFTER;
        }
        return null;
    }

    static Duration resolveTransientFailure(Throwable throwable) {
        Throwable rootCause = rootCause(throwable);
        if ("PrematureCloseException".equals(rootCause.getClass().getSimpleName())) {
            return DEFAULT_TRANSIENT_NETWORK_RETRY_AFTER;
        }
        return null;
    }

    private static Duration parse(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Duration.ofSeconds(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            try {
                return Duration.between(
                    ZonedDateTime.now(),
                    ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME)
                );
            } catch (DateTimeParseException ignoredDateFormat) {
                return null;
            }
        }
    }

    private static boolean isPositive(Duration duration) {
        return duration != null && !duration.isNegative() && !duration.isZero();
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root != null && root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root == null ? throwable : root;
    }
}
