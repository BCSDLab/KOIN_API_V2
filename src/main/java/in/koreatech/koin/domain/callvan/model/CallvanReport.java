package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportReasonCode;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "callvan_report")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callvan_post_id")
    private CallvanPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CallvanReportStatus status = CallvanReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<CallvanReportReason> reasons = new ArrayList<>();

    @Builder
    private CallvanReport(
        CallvanPost post,
        User reporter,
        User reported,
        String description,
        CallvanReportStatus status,
        User reviewer,
        String reviewNote,
        LocalDateTime reviewedAt,
        LocalDateTime confirmedAt,
        Boolean isDeleted
    ) {
        this.post = post;
        this.reporter = reporter;
        this.reported = reported;
        this.description = normalizeText(description);
        this.status = status != null ? status : CallvanReportStatus.PENDING;
        this.reviewer = reviewer;
        this.reviewNote = normalizeText(reviewNote);
        this.reviewedAt = reviewedAt;
        this.confirmedAt = confirmedAt;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public static CallvanReport create(
        CallvanPost post,
        User reporter,
        User reported
    ) {
        return CallvanReport.builder()
            .post(post)
            .reporter(reporter)
            .reported(reported)
            .status(CallvanReportStatus.PENDING)
            .build();
    }

    public void registerReasons(List<CallvanReportReasonCreateCommand> reasonCommands) {
        if (reasonCommands == null || reasonCommands.isEmpty()) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }

        Set<CallvanReportReasonCode> responseCodes = new HashSet<>();
        for (CallvanReportReasonCreateCommand reasonCommand : reasonCommands) {
            if (reasonCommand == null || reasonCommand.reasonCode() == null) {
                throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
            }

            if (!responseCodes.add(reasonCommand.reasonCode())) {
                throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
            }

            this.reasons.add(
                CallvanReportReason.create(this, reasonCommand.reasonCode(), reasonCommand.customText())
            );
        }
    }

    public void cancel() {
        this.status = CallvanReportStatus.CANCELED;
    }

    private static String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        return text.trim();
    }

    public record CallvanReportReasonCreateCommand(
        CallvanReportReasonCode reasonCode,
        String customText
    ) {
    }
}
