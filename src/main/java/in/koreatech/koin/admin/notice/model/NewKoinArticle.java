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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "new_koin_articles", schema = "koin")
@NoArgsConstructor(access = PROTECTED)
public class NewKoinArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "article_id", columnDefinition = "int UNSIGNED not null")
    private Long articleId;

    @Column(name = "user_id", columnDefinition = "int UNSIGNED not null")
    private Long userId;

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
    private NewKoinArticle(Integer id, Long articleId, Long userId, Boolean isDeleted, Instant createdAt,
        Instant updatedAt) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
