package in.koreatech.koin.domain.banner.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "banners")
@NoArgsConstructor(access = PROTECTED)
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "priority", nullable = false)
    private Integer priority;

    @NotNull
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "web_redirect_link", columnDefinition = "TEXT")
    private String webRedirectLink;

    @Column(name = "android_redirect_link", columnDefinition = "TEXT")
    private String androidRedirectLink;

    @Column(name = "ios_redirect_link", columnDefinition = "TEXT")
    private String iosRedirectLink;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JoinColumn(name = "banner_category_id")
    @ManyToOne(fetch = LAZY)
    private BannerCategory bannerCategory;

    @Builder
    private Banner(
        String title,
        Integer priority,
        String imageUrl,
        String webRedirectLink,
        String androidRedirectLink,
        String iosRedirectLink,
        Boolean isActive,
        BannerCategory bannerCategory
    ) {
        this.title = title;
        this.priority = priority;
        this.imageUrl = imageUrl;
        this.webRedirectLink = webRedirectLink;
        this.androidRedirectLink = androidRedirectLink;
        this.iosRedirectLink = iosRedirectLink;
        this.isActive = isActive;
        this.bannerCategory = bannerCategory;
    }
}
