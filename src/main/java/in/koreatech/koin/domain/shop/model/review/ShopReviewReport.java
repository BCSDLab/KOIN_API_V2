package in.koreatech.koin.domain.shop.model.review;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_review_reports")
@NoArgsConstructor(access = PROTECTED)
public class ShopReviewReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ShopReview review;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private ReportStatus reportStatus;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Student userId;

    @Builder
    public ShopReviewReport(
        ShopReview review,
        String title,
        String content,
        ReportStatus reportStatus,
        Student userId
    ) {
        this.review = review;
        this.title = title;
        this.content = content;
        this.reportStatus = reportStatus;
        this.userId = userId;
    }

    public void modifyReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }
}
