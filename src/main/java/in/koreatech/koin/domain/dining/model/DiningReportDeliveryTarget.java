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
@Table(name = "dining_soldout_report_delivery_target")
@NoArgsConstructor(access = PROTECTED)
public class DiningReportDeliveryTarget {

    public enum HoldReason { LEGACY, REJECTED, CONFLICT }

    @Id
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "desired_sequence", nullable = false)
    private Long desiredSequence;

    @Column(name = "desired_snapshot", nullable = false, columnDefinition = "LONGTEXT")
    private String desiredSnapshot;

    @Column(name = "confirmed_sequence", nullable = false)
    private Long confirmedSequence;

    @Column(name = "workspace_id", columnDefinition = "LONGTEXT")
    private String workspaceId;

    @Column(name = "channel_id", columnDefinition = "LONGTEXT")
    private String channelId;

    @Column(name = "message_ts", columnDefinition = "LONGTEXT")
    private String messageTs;

    @Column(name = "active_delivery_id", columnDefinition = "BINARY(16)")
    private UUID activeDeliveryId;

    @Enumerated(STRING)
    @Column(name = "hold_reason", length = 16)
    private HoldReason holdReason;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;

    public static DiningReportDeliveryTarget create(Integer reportId, long sequence, String snapshot,
        boolean legacy, LocalDateTime now) {
        DiningReportDeliveryTarget target = new DiningReportDeliveryTarget();
        target.reportId = reportId;
        target.desiredSequence = sequence;
        target.desiredSnapshot = snapshot;
        target.confirmedSequence = 0L;
        target.holdReason = legacy ? HoldReason.LEGACY : null;
        target.createdAt = now;
        target.updatedAt = now;
        return target;
    }

    public void recordDesired(long sequence, String snapshot, LocalDateTime now) {
        if (sequence <= desiredSequence) {
            throw new IllegalStateException("제보 전송 순번은 이전 상태보다 커야 합니다.");
        }
        desiredSequence = sequence;
        desiredSnapshot = snapshot;
        updatedAt = now;
    }

    public void captureRouting(String workspaceId, String channelId, LocalDateTime now) {
        if (this.workspaceId == null && this.channelId == null && holdReason == null) {
            this.workspaceId = workspaceId;
            this.channelId = channelId;
            updatedAt = now;
        }
    }

    public boolean hasRouting() {
        return workspaceId != null && channelId != null;
    }

    public void activate(UUID deliveryId, LocalDateTime now) {
        activeDeliveryId = deliveryId;
        updatedAt = now;
    }

    public void confirm(DiningReportDelivery delivery, String messageTs, LocalDateTime now) {
        // Only the issued snapshot is acknowledged, never the current desired snapshot.
        confirmedSequence = Math.max(confirmedSequence, delivery.getSourceSequence());
        if (this.messageTs == null) {
            this.messageTs = messageTs;
        }
        if (delivery.getId().equals(activeDeliveryId)) {
            activeDeliveryId = null;
        }
        updatedAt = now;
    }

    public void hold(HoldReason reason, LocalDateTime now) {
        holdReason = reason;
        updatedAt = now;
    }
}
