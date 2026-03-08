package in.koreatech.koin.domain.callvan.model.enums;

import java.time.LocalDateTime;

public enum CallvanReportProcessType {
    WARNING,
    TEMPORARY_RESTRICTION_14_DAYS,
    PERMANENT_RESTRICTION,
    REJECT;

    public boolean isRejected() {
        return this == REJECT;
    }

    public boolean isWarning() {
        return this == WARNING;
    }

    public LocalDateTime calculateRestrictedUntil(LocalDateTime processedAt) {
        if (this == TEMPORARY_RESTRICTION_14_DAYS) {
            return processedAt.plusDays(14);
        }
        return null;
    }
}
