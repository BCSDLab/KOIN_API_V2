package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportReasonCode;
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
@Table(name = "callvan_report_reason")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanReportReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private CallvanReport report;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 30)
    private CallvanReportReasonCode reasonCode;

    @Column(name = "custom_text", length = 200)
    private String customText;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private CallvanReportReason(
        CallvanReport report,
        CallvanReportReasonCode reasonCode,
        String customText,
        Boolean isDeleted
    ) {
        this.report = report;
        this.reasonCode = reasonCode;
        this.customText = customText;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public static CallvanReportReason create(
        CallvanReport report,
        CallvanReportReasonCode reasonCode,
        String customText
    ) {
        validateReasonDetail(reasonCode, customText);

        return CallvanReportReason.builder()
            .report(report)
            .reasonCode(reasonCode)
            .customText(normalizeCustomText(reasonCode, customText))
            .build();
    }

    private static void validateReasonDetail(CallvanReportReasonCode reasonCode, String customText) {
        if (reasonCode == null) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }

        if (reasonCode == CallvanReportReasonCode.OTHER && !StringUtils.hasText(customText)) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }

        if (reasonCode != CallvanReportReasonCode.OTHER && StringUtils.hasText(customText)) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }
    }

    private static String normalizeCustomText(CallvanReportReasonCode reasonCode, String customText) {
        if (reasonCode != CallvanReportReasonCode.OTHER) {
            return null;
        }

        String normalizedCustomText = customText.trim();
        if (normalizedCustomText.length() > 200) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }

        return normalizedCustomText;
    }
}
