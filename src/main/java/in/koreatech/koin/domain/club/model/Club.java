package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "club")
@NoArgsConstructor(access = PROTECTED)
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotNull
    @Size(max = 50)
    @Column(name = "normalized_name", length = 50, nullable = false)
    private String normalizedName;

    @NotNull
    @Column(name = "hits", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer hits = 0;

    @NotNull
    @Column(name = "last_week_hits", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer lastWeekHits = 0;

    @NotNull
    @Size(max = 100)
    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isActive = FALSE;

    @NotNull
    @Size(max = 255)
    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @NotNull
    @Column(name = "likes", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer likes;

    @NotNull
    @Size(max = 20)
    @Column(name = "location", length = 20, nullable = false)
    private String location;

    @NotNull
    @Column(name = "introduction", nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @NotNull
    @Column(name = "is_like_hidden", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isLikeHidden = FALSE;

    @JoinColumn(name = "club_category_id")
    @ManyToOne(fetch = LAZY)
    private ClubCategory clubCategory;

    @OneToMany(mappedBy = "club", orphanRemoval = true, cascade = ALL)
    private List<ClubManager> clubManagers = new ArrayList<>();

    @OneToMany(mappedBy = "club", orphanRemoval = true, cascade = ALL)
    private List<ClubSNS> clubSNSs = new ArrayList<>();

    @Transient
    public Boolean isManager;

    public void updateIsManager(Integer userId) {
        if (userId == null) {
            this.isManager = false;
            return;
        }

        this.isManager = clubManagers.stream()
            .anyMatch(clubManager -> clubManager.getUser().getId().equals(userId));
    }

    @Builder
    private Club(
        Integer id,
        String name,
        String normalizedName,
        Integer hits,
        Integer lastWeekHits,
        String description,
        Boolean isActive,
        String imageUrl,
        Integer likes,
        String location,
        String introduction,
        Boolean isLikeHidden,
        ClubCategory clubCategory
    ) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizedName;
        this.hits = hits;
        this.lastWeekHits = lastWeekHits;
        this.description = description;
        this.isActive = isActive;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.location = location;
        this.introduction = introduction;
        this.isLikeHidden = isLikeHidden;
        this.clubCategory = clubCategory;
    }

    public void updateLastWeekHits() {
        lastWeekHits = hits;
    }

    public int getHitsIncrease() {
        return hits - lastWeekHits;
    }

    public void increaseHits() {
        this.hits++;
    }

    public void modifyClub(
        String name,
        String imageUrl,
        ClubCategory clubCategory,
        String location,
        String description,
        Boolean isActive
    ) {
        this.name = name;
        this.normalizedName = normalized(name);
        this.imageUrl = imageUrl;
        this.clubCategory = clubCategory;
        this.location = location;
        this.description = description;
        this.isActive = isActive;
    }

    public void update(
        String name,
        String imageUrl,
        ClubCategory category,
        String location,
        String description,
        Boolean isLikeHidden
    ) {
        this.name = name;
        this.normalizedName = normalized(name);
        this.imageUrl = imageUrl;
        this.clubCategory = category;
        this.location = location;
        this.description = description;
        this.isLikeHidden = isLikeHidden;
    }

    private String normalized(String name) {
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    public void updateActive(Boolean active) {
        this.isActive = active;
    }

    public void increaseLikes() {
        this.likes++;
    }

    public void cancelLikes() {
        this.likes--;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
