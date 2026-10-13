package in.koreatech.koin.domain.dining.model;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_soldout_report_change", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dining_report_change_delivery", columnNames = "delivery_id"),
    @UniqueConstraint(name = "uk_dining_report_change_attempt", columnNames = "attempt_token")
})
@NoArgsConstructor(access = PROTECTED)
public class DiningReportChange {

    public enum EventType { CREATED, PROCESSED }

    @Id
    private Long sequence;

    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Enumerated(STRING)
    @Column(name = "event_type", nullable = false, length = 16)
    private EventType eventType;

    @Enumerated(STRING)
    @Column(nullable = false, length = 16)
    private DiningReportStatus status;

    @Enumerated(STRING)
    @Column(name = "processing_type", length = 32)
    private DiningReportProcessingType processingType;

    @Column(name = "processing_id", columnDefinition = "BINARY(16)")
    private UUID processingId;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime occurredAt;

    @Column(name = "delivery_id", columnDefinition = "BINARY(16)")
    private UUID deliveryId;

    @Column(name = "report_snapshot", columnDefinition = "LONGTEXT")
    private String reportSnapshot;

    @Enumerated(STRING)
    @Column(name = "delivery_state", nullable = false, length = 16)
    @ColumnDefault("'QUEUED'")
    private DiningReportDeliveryStatus deliveryState;

    @Column(name = "attempt_token", columnDefinition = "BINARY(16)")
    private UUID attemptToken;

    @Column(name = "expires_at", columnDefinition = "DATETIME(6)")
    @Convert(disableConversion = true)
    private LocalDateTime expiresAt;

    @Column(name = "next_attempt_at", columnDefinition = "DATETIME(6)")
    @Convert(disableConversion = true)
    private LocalDateTime nextAttemptAt;

    @Enumerated(STRING)
    @Column(name = "accepted_outcome", length = 16)
    private DiningReportDeliveryOutcome acceptedOutcome;

    public static DiningReportChange from(long sequence, DiningReport report, EventType eventType,
        LocalDateTime now, String snapshot) {
        DiningReportChange change = new DiningReportChange();
        change.sequence = sequence;
        change.reportId = report.getId();
        change.eventType = eventType;
        change.status = report.getStatus();
        change.processingType = report.getProcessingType();
        change.processingId = report.getProcessingId();
        change.occurredAt = now;
        change.deliveryState = DiningReportDeliveryStatus.QUEUED;
        change.initializeDelivery(snapshot);
        return change;
    }

    public void initializeDelivery(String snapshot) {
        if (deliveryId == null) {
            deliveryId = UUID.randomUUID();
        }
        if (reportSnapshot == null) {
            reportSnapshot = Objects.requireNonNull(snapshot, "제보 작업 내용이 없습니다.");
        }
    }

    public void issue(LocalDateTime now) {
        attemptToken = UUID.randomUUID();
        expiresAt = now.plusSeconds(60);
        nextAttemptAt = null;
        acceptedOutcome = null;
        deliveryState = DiningReportDeliveryStatus.IN_PROGRESS;
    }

    public void accept(DiningReportDeliveryOutcome outcome, LocalDateTime now) {
        acceptedOutcome = outcome;
        if (outcome == DiningReportDeliveryOutcome.SUCCEEDED) {
            deliveryState = DiningReportDeliveryStatus.DELIVERED;
            nextAttemptAt = null;
        } else {
            deliveryState = DiningReportDeliveryStatus.QUEUED;
            nextAttemptAt = now.plusSeconds(5);
        }
    }
}
