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
@Table(name = "dining_soldout_report_change")
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

    public static DiningReportChange from(long sequence, DiningReport report, EventType eventType,
        LocalDateTime now) {
        DiningReportChange change = new DiningReportChange();
        change.sequence = sequence;
        change.reportId = report.getId();
        change.eventType = eventType;
        change.status = report.getStatus();
        change.processingType = report.getProcessingType();
        change.processingId = report.getProcessingId();
        change.occurredAt = now;
        return change;
    }
}
