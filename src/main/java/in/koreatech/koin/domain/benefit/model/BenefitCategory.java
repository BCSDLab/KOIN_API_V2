package in.koreatech.koin.domain.benefit.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop_benefit_categories")
@NoArgsConstructor(access = PROTECTED)
public class BenefitCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(min = 5, max = 20)
    @Column(name = "title", nullable = false)
    String title;

    @Size(min = 10, max = 100)
    @Column(name = "detail")
    String detail;

    @Column(name = "on_image_url")
    String onImageUrl;

    @Column(name = "off_image_url")
    String offImageUrl;

    public static final int MAX_BENEFIT_CATEGORIES = 6;
    public static final int MIN_BENEFIT_CATEGORIES = 2;

    @Builder
    public BenefitCategory(String title, String detail, String onImageUrl, String offImageUrl) {
        this.title = title;
        this.detail = detail;
        this.onImageUrl = onImageUrl;
        this.offImageUrl = offImageUrl;
    }

    public void update(String title, String detail, String onImageUrl, String offImageUrl) {
        this.title = title;
        this.detail = detail;
        this.onImageUrl = onImageUrl;
        this.offImageUrl = offImageUrl;
    }

    public static boolean isExceededMaxCategoryCount(int currentCategoryCount) {
        return currentCategoryCount >= MAX_BENEFIT_CATEGORIES;
    }

    public static boolean isBelowMinCategoryCount(int currentCategoryCount) {
        return currentCategoryCount <= MIN_BENEFIT_CATEGORIES;
    }
}
