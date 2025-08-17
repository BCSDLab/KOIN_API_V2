package in.koreatech.koin.domain.shop.model.review;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_review_reports_categories")
@NoArgsConstructor(access = PROTECTED)
public class ShopReviewReportCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "detail", nullable = false, length = 255)
    private String detail;

    @Builder
    public ShopReviewReportCategory(String name, String detail) {
        this.name = name;
        this.detail = detail;
    }
}
