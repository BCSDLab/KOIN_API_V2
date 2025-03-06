package in.koreatech.koin.domain.community.article.model;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lost_item_images", schema = "koin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItemImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", referencedColumnName = "id", nullable = false)
    private LostItemArticle lostItemArticle;

    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public LostItemImage(
        Integer id,
        LostItemArticle lostItemArticle,
        String imageUrl,
        Boolean isDeleted
    ) {
        this.id = id;
        this.lostItemArticle = lostItemArticle;
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
    }

    public void setArticle(LostItemArticle lostItemArticle) {
        this.lostItemArticle = lostItemArticle;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
