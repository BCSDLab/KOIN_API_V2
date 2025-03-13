package in.koreatech.koin.domain.member.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tech_stacks")
@NoArgsConstructor(access = PROTECTED)
public class TechStack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "track_id")
    private Integer trackId;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private TechStack(
        String imageUrl,
        String name,
        String description,
        Integer trackId,
        boolean isDeleted
    ) {
        this.imageUrl = imageUrl;
        this.trackId = trackId;
        this.name = name;
        this.description = description;
        this.isDeleted = isDeleted;
    }

    public void update(Integer trackId, String imageUrl, String name, String description, boolean isDeleted) {
        this.imageUrl = imageUrl;
        this.trackId = trackId;
        this.name = name;
        this.description = description;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
