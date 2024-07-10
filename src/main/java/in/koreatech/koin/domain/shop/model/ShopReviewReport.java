package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "shop_review_reports")
@NoArgsConstructor(access = PROTECTED)
public class ShopReviewReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ShopReview review;

    @Column(name = "reason_title", nullable = false, length = 50)
    private String reasonTitle;

    @Column(name = "reason_detail", nullable = false, length = 255)
    private String reasonDetail;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private Student reportedBy;

    @Builder
    public ShopReviewReport(ShopReview review, String reasonTitle, String reasonDetail, Student reportedBy) {
        this.review = review;
        this.reasonTitle = reasonTitle;
        this.reasonDetail = reasonDetail;
        this.reportedBy = reportedBy;
    }
}
