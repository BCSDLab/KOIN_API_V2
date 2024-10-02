package in.koreatech.koin.admin.notice.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "new_koreatech_articles", schema = "koin")
@NoArgsConstructor(access = PROTECTED)
public class NewKoreatechArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "article_id", columnDefinition = "int UNSIGNED not null")
    private Long articleId;

    @Size(max = 50)
    @NotNull
    @Column(name = "author", nullable = false, length = 50)
    private String author;

    @Column(name = "portal_num", columnDefinition = "int UNSIGNED not null")
    private Long portalNum;

    @Size(max = 255)
    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "registered_at")
    private Instant registeredAt;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private NewKoreatechArticle(Long id, Long articleId, String author, Long portalNum, String url,
        Instant registeredAt,
        Boolean isDeleted, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.articleId = articleId;
        this.author = author;
        this.portalNum = portalNum;
        this.url = url;
        this.registeredAt = registeredAt;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
