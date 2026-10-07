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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_soldout_report_delivery", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dining_delivery_snapshot", columnNames = {"report_id", "source_sequence"})
})
@NoArgsConstructor(access = PROTECTED)
public class DiningReportDelivery {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "report_id", nullable = false, updatable = false)
    private Integer reportId;

    @Column(name = "source_sequence", nullable = false, updatable = false)
    private Long sourceSequence;

    @Column(name = "report_snapshot", nullable = false, updatable = false, columnDefinition = "LONGTEXT")
    private String reportSnapshot;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private DiningReportDeliveryOperation operation;

    @Column(name = "workspace_id", nullable = false, updatable = false, columnDefinition = "LONGTEXT")
    private String workspaceId;

    @Column(name = "channel_id", nullable = false, updatable = false, columnDefinition = "LONGTEXT")
    private String channelId;

    @Column(name = "target_message_ts", updatable = false, columnDefinition = "LONGTEXT")
    private String targetMessageTs;

    @Enumerated(STRING)
    @Column(nullable = false, length = 16)
    private DiningReportDeliveryStatus status;

    @Column(name = "active_attempt_token", columnDefinition = "BINARY(16)")
    private UUID activeAttemptToken;

    @Column(name = "send_attempt_token", columnDefinition = "BINARY(16)")
    private UUID sendAttemptToken;

    @Column(name = "next_attempt_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime nextAttemptAt;

    @Column(name = "confirmed_message_ts", columnDefinition = "LONGTEXT")
    private String confirmedMessageTs;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;

    public static DiningReportDelivery create(DiningReportDeliveryTarget target, LocalDateTime now) {
        DiningReportDelivery delivery = new DiningReportDelivery();
        delivery.id = UUID.randomUUID();
        delivery.reportId = target.getReportId();
        delivery.sourceSequence = target.getDesiredSequence();
        delivery.reportSnapshot = target.getDesiredSnapshot();
        delivery.operation = target.getMessageTs() == null
            ? DiningReportDeliveryOperation.CREATE : DiningReportDeliveryOperation.UPDATE;
        delivery.workspaceId = target.getWorkspaceId();
        delivery.channelId = target.getChannelId();
        delivery.targetMessageTs = target.getMessageTs();
        delivery.status = DiningReportDeliveryStatus.QUEUED;
        delivery.nextAttemptAt = now;
        delivery.createdAt = now;
        delivery.updatedAt = now;
        return delivery;
    }

    public void issue(DiningReportDeliveryAttempt attempt, LocalDateTime now) {
        activeAttemptToken = attempt.getToken();
        if (attempt.getMode() == DiningReportDeliveryMode.SEND) {
            sendAttemptToken = attempt.getToken();
        }
        status = DiningReportDeliveryStatus.IN_PROGRESS;
        nextAttemptAt = null;
        updatedAt = now;
    }

    public void awaitVerification(LocalDateTime now) {
        activeAttemptToken = null;
        status = DiningReportDeliveryStatus.UNCERTAIN;
        nextAttemptAt = now;
        updatedAt = now;
    }

    public void queue(LocalDateTime nextAttemptAt, LocalDateTime now) {
        activeAttemptToken = null;
        status = DiningReportDeliveryStatus.QUEUED;
        this.nextAttemptAt = nextAttemptAt;
        updatedAt = now;
    }

    public void needsAttention(LocalDateTime nextVerificationAt, LocalDateTime now) {
        activeAttemptToken = null;
        status = DiningReportDeliveryStatus.NEEDS_ATTENTION;
        nextAttemptAt = nextVerificationAt;
        updatedAt = now;
    }

    public void succeed(String messageTs, LocalDateTime now) {
        activeAttemptToken = null;
        status = DiningReportDeliveryStatus.DELIVERED;
        nextAttemptAt = null;
        if (confirmedMessageTs == null) {
            confirmedMessageTs = messageTs;
        }
        updatedAt = now;
    }
}
