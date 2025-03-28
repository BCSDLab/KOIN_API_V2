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
import jakarta.validation.constraints.Size;
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

    @Column(name = "priority")
    private Integer priority;

    @NotNull
    @Size(max = 255)
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "web_redirect_link")
    private String webRedirectLink;

    @Size(max = 255)
    @Column(name = "android_redirect_link")
    private String androidRedirectLink;

    @Size(max = 50)
    @Column(name = "android_minimum_version")
    private String androidMinimumVersion;

    @Size(max = 255)
    @Column(name = "ios_redirect_link")
    private String iosRedirectLink;

    @Size(max = 50)
    @Column(name = "ios_minimum_version")
    private String iosMinimumVersion;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JoinColumn(name = "banner_category_id")
    @ManyToOne(fetch = LAZY)
    private BannerCategory bannerCategory;

    @Builder
    public Banner(
        String title,
        Integer priority,
        String imageUrl,
        String webRedirectLink,
        String androidRedirectLink,
        String androidMinimumVersion,
        String iosRedirectLink,
        String iosMinimumVersion,
        Boolean isActive,
        BannerCategory bannerCategory
    ) {
        this.title = title;
        this.priority = priority;
        this.imageUrl = imageUrl;
        this.webRedirectLink = webRedirectLink;
        this.androidRedirectLink = androidRedirectLink;
        this.androidMinimumVersion = androidMinimumVersion;
        this.iosRedirectLink = iosRedirectLink;
        this.iosMinimumVersion = iosMinimumVersion;
        this.isActive = isActive;
        this.bannerCategory = bannerCategory;
    }

    public void modifyBanner(
        String title,
        String imageUrl,
        String webRedirectLink,
        String androidRedirectLink,
        String iosRedirectLink
    ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.webRedirectLink = webRedirectLink;
        this.androidRedirectLink = androidRedirectLink;
        this.iosRedirectLink = iosRedirectLink;
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
    }

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
