package in.koreatech.koin.admin.notice.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "new_articles", schema = "koin")
@NoArgsConstructor(access = PROTECTED)
public class NewArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "board_id", columnDefinition = "int UNSIGNED not null")
    private Long boardId;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @ColumnDefault("'0'")
    @Column(name = "hit", columnDefinition = "int UNSIGNED not null")
    private Long hit;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice = false;

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
    private NewArticle(Long id, Long boardId, String title, String content, Long hit, Boolean isNotice,
        Boolean isDeleted,
        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.hit = hit;
        this.isNotice = isNotice;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
