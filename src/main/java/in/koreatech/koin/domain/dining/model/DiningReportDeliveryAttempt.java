package in.koreatech.koin.domain.dining.model;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_soldout_report_delivery_attempt")
@NoArgsConstructor(access = PROTECTED)
public class DiningReportDeliveryAttempt {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID token;

    @Column(name = "delivery_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID deliveryId;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private DiningReportDeliveryMode mode;

    @Column(name = "send_attempt_token", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID sendAttemptToken;

    @Column(name = "issued_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime expiresAt;

    @Column(name = "expired_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime expiredAt;

    @Column(name = "invalidated_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime invalidatedAt;

    @Enumerated(STRING)
    @Column(name = "accepted_outcome", length = 16)
    private DiningReportDeliveryOutcome acceptedOutcome;

    @Column(name = "accepted_result", columnDefinition = "LONGTEXT")
    private String acceptedResult;

    @Column(name = "original_result", columnDefinition = "LONGTEXT")
    private String originalResult;

    @Column(name = "evidence_history", nullable = false, columnDefinition = "LONGTEXT")
    private String evidenceHistory;

    @Column(name = "result_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime resultAt;

    public static DiningReportDeliveryAttempt issue(DiningReportDelivery delivery, DiningReportDeliveryMode mode,
        LocalDateTime now) {
        DiningReportDeliveryAttempt attempt = new DiningReportDeliveryAttempt();
        attempt.token = UUID.randomUUID();
        attempt.deliveryId = delivery.getId();
        attempt.mode = mode;
        attempt.sendAttemptToken = mode == DiningReportDeliveryMode.SEND
            ? attempt.token : delivery.getSendAttemptToken();
        attempt.issuedAt = now;
        attempt.expiresAt = now.plusSeconds(60);
        attempt.evidenceHistory = "[]";
        return attempt;
    }

    public void recordEvidence(String result, String history) {
        if (originalResult == null) {
            originalResult = result;
        }
        evidenceHistory = history;
    }

    public void accept(DiningReportDeliveryOutcome outcome, String result, LocalDateTime now) {
        acceptedOutcome = outcome;
        acceptedResult = result;
        resultAt = now;
    }

    public boolean hasDefiniteOutcome() {
        return acceptedOutcome != null && acceptedOutcome != DiningReportDeliveryOutcome.UNCERTAIN;
    }

    public void expire(LocalDateTime now) {
        if (expiredAt == null) {
            expiredAt = now;
        }
    }

    public void invalidate(LocalDateTime now) {
        if (invalidatedAt == null) {
            invalidatedAt = now;
        }
    }
}
