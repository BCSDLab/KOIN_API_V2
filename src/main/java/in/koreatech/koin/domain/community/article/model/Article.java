package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.global.config.LocalDateAttributeConverter;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "koreatech_articles", uniqueConstraints = {
    @UniqueConstraint(name = "ux_koreatech_article", columnNames = {"board_id", "article_num"})
})
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, updatable = false)
    private Board board;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false, updatable = false)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false, updatable = false)
    private String content;

    @Size(max = 50)
    @NotNull
    @Column(name = "author", nullable = false, length = 50, updatable = false)
    private String author;

    @NotNull
    @Column(name = "hit", nullable = false)
    private int hit;

    @NotNull
    @Column(name = "koin_hit", nullable = false)
    private int koinHit;

    @NotNull
    @Column(name = "is_deleted", nullable = false, updatable = false)
    private boolean isDeleted = false;

    @Column(name = "article_num", nullable = false, updatable = false)
    private Integer articleNum;

    @Column(name = "url", nullable = false, updatable = false)
    private String url;

    @Convert(converter = LocalDateAttributeConverter.class)
    @Column(name = "registered_at", columnDefinition = "DATETIME", updatable = false)
    private LocalDate registeredAt;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", updatable = false)
    private List<ArticleAttachment> attachments = new ArrayList<>();

    @NotNull
    @Column(name = "is_notice", nullable = false, updatable = false)
    private boolean isNotice = false;

    @Transient
    private Integer prevId;

    @Transient
    private Integer nextId;

    public void increaseKoinHit() {
        koinHit++;
    }

    public void setPrevNextArticles(Article prev, Article next) {
        if (prev != null) {
            prevId = prev.getId();
        }
        if (next != null) {
            nextId = next.getId();
        }
    }

    @Builder
    private Article(
        Board board,
        String title,
        String content,
        String author,
        Integer hit,
        Integer koinHit,
        boolean isDeleted,
        Integer articleNum,
        String url,
        LocalDate registeredAt,
        List<ArticleAttachment> attachments,
        boolean isNotice
    ) {
        this.board = board;
        this.title = title;
        this.content = content;
        this.author = author;
        this.hit = hit;
        this.koinHit = koinHit;
        this.isDeleted = isDeleted;
        this.articleNum = articleNum;
        this.url = url;
        this.registeredAt = registeredAt;
        this.attachments = attachments;
        this.isNotice = isNotice;
    }
}
