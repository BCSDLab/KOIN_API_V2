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

    @NotNull
    @Column(name = "is_web_released", nullable = false)
    private Boolean isWebReleased;

    @Size(max = 255)
    @Column(name = "web_redirect_link")
    private String webRedirectLink;

    @NotNull
    @Column(name = "is_android_released", nullable = false)
    private Boolean isAndroidReleased;

    @Size(max = 255)
    @Column(name = "android_redirect_link")
    private String androidRedirectLink;

    @Size(max = 50)
    @Column(name = "android_minimum_version")
    private String androidMinimumVersion;

    @NotNull
    @Column(name = "is_ios_released", nullable = false)
    private Boolean isIosReleased;

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
        Boolean isWebReleased,
        String webRedirectLink,
        Boolean isAndroidReleased,
        String androidRedirectLink,
        String androidMinimumVersion,
        Boolean isIosReleased,
        String iosRedirectLink,
        String iosMinimumVersion,
        Boolean isActive,
        BannerCategory bannerCategory
    ) {
        this.title = title;
        this.priority = priority;
        this.imageUrl = imageUrl;
        this.isWebReleased = isWebReleased;
        this.webRedirectLink = webRedirectLink;
        this.isAndroidReleased = isAndroidReleased;
        this.androidRedirectLink = androidRedirectLink;
        this.androidMinimumVersion = androidMinimumVersion;
        this.isIosReleased = isIosReleased;
        this.iosRedirectLink = iosRedirectLink;
        this.iosMinimumVersion = iosMinimumVersion;
        this.isActive = isActive;
        this.bannerCategory = bannerCategory;
    }

    public void modifyBanner(
        String title,
        String imageUrl,
        Boolean isWebReleased,
        String webRedirectLink,
        Boolean isAndroidReleased,
        String androidRedirectLink,
        String androidMinimumVersion,
        Boolean isIosReleased,
        String iosRedirectLink,
        String iosMinimumVersion
    ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.isWebReleased = isWebReleased;
        this.webRedirectLink = webRedirectLink;
        this.isAndroidReleased = isAndroidReleased;
        this.androidRedirectLink = androidRedirectLink;
        this.androidMinimumVersion = androidMinimumVersion;
        this.isIosReleased = isIosReleased;
        this.iosRedirectLink = iosRedirectLink;
        this.iosMinimumVersion = iosMinimumVersion;
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
    }

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
