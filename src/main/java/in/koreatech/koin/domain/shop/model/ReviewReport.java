package in.koreatech.koin.domain.shop.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_review_reports")
@NoArgsConstructor(access = PROTECTED)
public class ReviewReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private ShopReview review;

    @Column(name = "reason_title", nullable = false, length = 50)
    private String reasonTitle;

    @Column(name = "reason_detail", nullable = false, length = 255)
    private String reasonDetail;

    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;
}
