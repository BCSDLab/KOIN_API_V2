package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
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
    @Column(name = "hits", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer hits = 0;

    @NotNull
    @Size(max = 100)
    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean active = FALSE;

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

    @JoinColumn(name = "club_category_id")
    @ManyToOne(fetch = LAZY)
    private ClubCategory clubCategory;

    @Builder
    private Club(
        Integer id,
        String name,
        Integer hits,
        String description,
        Boolean active,
        String imageUrl,
        Integer likes,
        String location,
        String introduction,
        ClubCategory clubCategory
    ) {
        this.id = id;
        this.name = name;
        this.hits = hits;
        this.description = description;
        this.active = active;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.location = location;
        this.introduction = introduction;
        this.clubCategory = clubCategory;
    }
}
