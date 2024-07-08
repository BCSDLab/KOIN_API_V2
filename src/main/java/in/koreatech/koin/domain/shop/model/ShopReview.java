package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.CascadeType.ALL;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
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
@Table(name = "shop_reviews")
@NoArgsConstructor(access = PROTECTED)
public class ShopReview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = ALL)
    private List<ShopReviewImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = ALL)
    private List<ShopReviewMenu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = ALL)
    private List<ShopReviewReport> reports = new ArrayList<>();

    @Builder
    public ShopReview(String content, Integer rating, User reviewer, Shop shop) {
        this.content = content;
        this.rating = rating;
        this.reviewer = reviewer;
        this.shop = shop;
    }

    public void modifyReview(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void modifyReviewImage(List<String> imageUrls, EntityManager entityManager) {
        this.images.clear();
        entityManager.flush();
        for (String imageUrl : imageUrls) {
            ShopReviewImage shopReviewImage = ShopReviewImage.builder()
                .imageUrls(imageUrl)
                .review(this)
                .build();
            images.add(shopReviewImage);
        }
    }

    public void modifyMenuName(List<String> menuNames, EntityManager entityManager) {
        this.menus.clear();
        entityManager.flush();
        for (String menuName : menuNames) {
            ShopReviewMenu shopReviewImage = ShopReviewMenu.builder()
                    .menuName(menuName)
                    .review(this)
                    .build();
            this.menus.add(shopReviewImage);
        }
    }
}
