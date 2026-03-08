package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "callvan_report_process")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanReportProcess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private CallvanReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    private User processor;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_type", nullable = false, length = 50)
    private CallvanReportProcessType processType;

    @Column(name = "restricted_until", columnDefinition = "DATETIME")
    private LocalDateTime restrictedUntil;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private CallvanReportProcess(
        CallvanReport report,
        User processor,
        CallvanReportProcessType processType,
        LocalDateTime restrictedUntil,
        Boolean isDeleted
    ) {
        this.report = report;
        this.processor = processor;
        this.processType = processType;
        this.restrictedUntil = restrictedUntil;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public static CallvanReportProcess create(
        CallvanReport report,
        User processor,
        CallvanReportProcessType processType,
        LocalDateTime restrictedUntil
    ) {
        return CallvanReportProcess.builder()
            .report(report)
            .processor(processor)
            .processType(processType)
            .restrictedUntil(restrictedUntil)
            .build();
    }
}
