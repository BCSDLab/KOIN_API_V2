package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportAttachmentType;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
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
@Table(name = "callvan_report_attachment")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanReportAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private CallvanReport report;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 30)
    private CallvanReportAttachmentType attachmentType;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public CallvanReportAttachment(CallvanReport report, CallvanReportAttachmentType attachmentType, String url,
        Boolean isDeleted) {
        this.report = report;
        this.attachmentType = attachmentType;
        this.url = url;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public static CallvanReportAttachment create(
        CallvanReport report, CallvanReportAttachmentType attachmentType, String url
    ) {
        validateAttachment(attachmentType, url);

        return CallvanReportAttachment.builder()
            .report(report)
            .attachmentType(attachmentType)
            .url(url.trim())
            .build();
    }

    private static void validateAttachment(CallvanReportAttachmentType attachmentType, String url) {
        if (attachmentType == null || !StringUtils.hasText(url) || url.trim().length() > 500) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }
    }
}
