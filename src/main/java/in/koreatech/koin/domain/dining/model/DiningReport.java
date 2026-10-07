package in.koreatech.koin.domain.dining.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "dining_soldout_report", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dining_report_student", columnNames = {"reporter_id", "dining_id"}),
    @UniqueConstraint(name = "uk_dining_report_request", columnNames = {"reporter_id", "request_key"})
})
public class DiningReport {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "dining_id", nullable = false)
    private Dining dining;

    @Column(name = "reporter_id")
    private Integer reporterId;

    @Column(name = "image_url", length = 2048, nullable = false)
    private String imageUrl;

    @Enumerated(STRING)
    @Column(nullable = false, length = 16)
    private DiningReportStatus status;

    @Enumerated(STRING)
    @Column(name = "processing_type", length = 32)
    private DiningReportProcessingType processingType;

    @Column(name = "processing_id", columnDefinition = "BINARY(16)")
    private UUID processingId;

    @Column(name = "source_report_id")
    private Integer sourceReportId;

    @Column(name = "processor_workspace_id", length = 64)
    private String processorWorkspaceId;

    @Column(name = "processor_user_id", length = 64)
    private String processorUserId;

    @Column(name = "processor_name", length = 80)
    private String processorName;

    @Column(name = "processed_at", columnDefinition = "DATETIME")
    private LocalDateTime processedAt;

    @Column(name = "request_key", columnDefinition = "BINARY(16)", nullable = false)
    private UUID requestKey;

    @Column(name = "created_at", columnDefinition = "DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime updatedAt;

    public static DiningReport create(Dining dining, Integer reporterId, String imageUrl,
        UUID requestKey, LocalDateTime now) {
        DiningReport report = new DiningReport();
        report.dining = dining;
        report.reporterId = reporterId;
        report.imageUrl = imageUrl;
        report.requestKey = requestKey;
        report.status = DiningReportStatus.PENDING;
        report.createdAt = now;
        report.updatedAt = now;
        return report;
    }

    public void process(DiningReportStatus result, DiningReportProcessingType type, UUID batchId,
        Integer sourceId, LocalDateTime now, String workspaceId, String userId, String displayName) {
        if (status != DiningReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 제보는 변경할 수 없습니다.");
        }
        status = result;
        processingType = type;
        processingId = batchId;
        sourceReportId = sourceId;
        processedAt = now;
        updatedAt = now;
        processorWorkspaceId = workspaceId;
        processorUserId = userId;
        processorName = displayName;
    }

    public String getReason() {
        if (processingType == null) {
            return null;
        }
        return switch (processingType) {
            case MANUAL -> status == DiningReportStatus.APPROVED ? "담당자 승인" : "담당자 반려";
            case SAME_DINING_APPROVED -> "동일 코스 제보 승인에 따른 자동 처리";
            case COOP_PREPROCESSED -> "영양사 선처리로 제보 확인 없이 종료";
        };
    }
}
