package in.koreatech.koin.domain.community.article.model;

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
@Table(name = "lost_item_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItemArticle lostItemArticle;

    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    public LostItemImage(
        Integer id,
        LostItemArticle lostItemArticle,
        String imageUrl
    ) {
        this.id = id;
        this.lostItemArticle = lostItemArticle;
        this.imageUrl = imageUrl;
    }

    public void setArticle(LostItemArticle lostItemArticle) {
        this.lostItemArticle = lostItemArticle;
    }
}