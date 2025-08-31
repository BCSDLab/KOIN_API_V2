package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import in.koreatech.koin.common.converter.LocalDateAttributeConverter;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "new_koreatech_articles", schema = "koin")
@NoArgsConstructor(access = PROTECTED)
public class KoreatechArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Size(max = 50)
    @NotNull
    @Column(name = "author", nullable = false, length = 50, updatable = false)
    private String author;

    @Column(name = "portal_num", updatable = false)
    private int portalNum;

    @Column(name = "portal_hit", nullable = false, updatable = false)
    private int portalHit;

    @Size(max = 255)
    @NotNull
    @Column(name = "url", nullable = false, updatable = false)
    private String url;

    @Convert(converter = LocalDateAttributeConverter.class)
    @Column(name = "registered_at", columnDefinition = "DATETIME", updatable = false)
    private LocalDate registeredAt;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public KoreatechArticle(
        Integer id,
        Article article,
        String author,
        int portalNum,
        int portalHit,
        String url,
        LocalDate registeredAt,
        Boolean isDeleted
    ) {
        this.id = id;
        this.article = article;
        this.author = author;
        this.portalNum = portalNum;
        this.portalHit = portalHit;
        this.url = url;
        this.registeredAt = registeredAt;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
