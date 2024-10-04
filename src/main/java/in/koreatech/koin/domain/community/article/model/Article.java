package in.koreatech.koin.domain.community.article.model;

import static in.koreatech.koin.domain.community.article.model.Board.KOIN_ADMIN_NOTICE_BOARD_ID;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Where;

import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "new_articles")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class Article extends BaseEntity {

    private static final String ADMIN_NOTICE_AUTHOR = "BCSD Lab";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id", nullable = false, updatable = false)
    private Board board;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "hit", nullable = false)
    private Integer hit = 0;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "article_id", updatable = false)
    private List<ArticleAttachment> attachments = new ArrayList<>();

    @NotNull
    @Column(name = "is_notice", nullable = false, updatable = false)
    private boolean isNotice = false;

    @OneToOne(mappedBy = "article", fetch = LAZY, cascade = ALL)
    private KoreatechArticle koreatechArticle;

    @OneToOne(mappedBy = "article", fetch = LAZY, cascade = ALL)
    private KoinArticle koinArticle;

    @Transient
    private Integer prevId;

    @Transient
    private Integer nextId;

    public void increaseKoinHit() {
        this.hit++;
    }

    public int getTotalHit() {
        if (this.koreatechArticle != null) {
            return this.koreatechArticle.getPortalHit() + this.hit;
        }
        return this.hit;
    }

    public void setPrevNextArticles(Article prev, Article next) {
        if (prev != null) {
            prevId = prev.getId();
        }
        if (next != null) {
            nextId = next.getId();
        }
    }

    public String getAuthor() {
        if (this.koreatechArticle != null || this.koinArticle == null) {
            return this.koreatechArticle.getAuthor();
        } else if (Objects.equals(this.board.getId(), KOIN_ADMIN_NOTICE_BOARD_ID)) {
            return ADMIN_NOTICE_AUTHOR;
        } else {
            if (Objects.equals(this.koinArticle.getUser(), null)) {
                return "탈퇴한 사용자";
            }
            return this.koinArticle.getUser().getName();
        }
    }

    public LocalDate getRegisteredAt() {
        if (this.koreatechArticle != null) {
            return this.koreatechArticle.getRegisteredAt();
        } else
            return null;
    }

    public int getArticleNum() {
        return this.koreatechArticle.getPortalNum();
    }

    public String getUrl() {
        return this.koreatechArticle.getUrl();
    }

    public void delete() {
        this.isDeleted = true;
        if (this.koinArticle != null) {
            this.koinArticle.delete();
        }
        if (this.koreatechArticle != null) {
            this.koreatechArticle.delete();
        }
    }

    public void updateKoinAdminArticle(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Builder
    public Article(
        Integer id,
        Board board,
        String title,
        String content,
        int hit,
        boolean isDeleted,
        List<ArticleAttachment> attachments,
        boolean isNotice,
        KoreatechArticle koreatechArticle,
        KoinArticle koinArticle
    ) {
        this.id = id;
        this.board = board;
        this.title = title;
        this.content = content;
        this.hit = hit;
        this.isDeleted = isDeleted;
        this.attachments = attachments;
        this.isNotice = isNotice;
        this.koreatechArticle = koreatechArticle;
        this.koinArticle = koinArticle;
    }

    public static Article createKoinNoticeArticleByAdmin(
        AdminNoticeRequest request,
        Board adminNoticeBoard,
        User adminUser
    ) {
        KoinArticle koinArticle = KoinArticle.builder()
            .user(adminUser)
            .isDeleted(false)
            .build();

        Article article = Article.builder()
            .board(adminNoticeBoard)
            .title(request.title())
            .content(request.content())
            .isNotice(true)
            .koinArticle(koinArticle)
            .koreatechArticle(null)
            .isDeleted(false)
            .build();

        koinArticle.setArticle(article);
        return article;
    }
}
