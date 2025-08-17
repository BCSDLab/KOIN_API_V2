package in.koreatech.koin.domain.shop.model.review;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
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
@Table(name = "shop_review_menus")
@NoArgsConstructor(access = PROTECTED)
public class ShopReviewMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ShopReview review;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Builder
    public ShopReviewMenu(ShopReview review, String menuName) {
        this.review = review;
        this.menuName = menuName;
    }
}
