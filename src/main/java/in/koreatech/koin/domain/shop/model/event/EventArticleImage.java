package in.koreatech.koin.domain.shop.model.event;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Getter
@Entity
@Table(name = "event_article_thumbnail_images")
@NoArgsConstructor(access = PROTECTED)
public class EventArticleImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventArticle eventArticle;

    @Size(max = 255)
    @NotNull
    @Column(name = "thumbnail_image")
    private String thumbnailImage;

    @Builder
    private EventArticleImage(
        EventArticle eventArticle,
        String thumbnailImage
    ) {
        this.eventArticle = eventArticle;
        this.thumbnailImage = thumbnailImage;
    }
}
