package in.koreatech.koin.domain.community.article.model;

import static in.koreatech.koin.domain.community.article.model.Board.KOIN_NOTICE_BOARD_ID;
import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.admin.notice.model.KoinNotice;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleRequest;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
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
    private static final String DELETED_USER = "탈퇴한 사용자";
    private static final String ANONYMOUS_USER = "익명";

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

    @Column(name = "content")
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

    @OneToOne(mappedBy = "article", fetch = LAZY, cascade = ALL)
    private LostItemArticle lostItemArticle;

    @OneToOne(mappedBy = "article", fetch = LAZY, cascade = ALL)
    private KoinNotice koinNotice;

    @Transient
    private Integer prevId;

    @Transient
    private Integer nextId;

    @Transient
    private String author;

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

    @PostPersist
    @PostLoad
    public void updateAuthor() {
        if (Objects.equals(board.getId(), KOIN_NOTICE_BOARD_ID)) {
            author = ADMIN_NOTICE_AUTHOR;
            return;
        }
        if (koinArticle != null) {
            author = (koinArticle.getUser() != null) ? koinArticle.getUser().getName() : DELETED_USER;
            return;
        }
        if (koreatechArticle != null) {
            author = (koreatechArticle.getAuthor() != null) ? koreatechArticle.getAuthor() : DELETED_USER;
            return;
        }
        if (lostItemArticle != null) {
            User user = lostItemArticle.getAuthor();
            if (user != null) {
                author = user.getNickname() != null ? user.getNickname() : ANONYMOUS_USER;
                return;
            }
        }
        author = DELETED_USER;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getRegisteredAt() {
        if (this.koreatechArticle != null) {
            return this.koreatechArticle.getRegisteredAt();
        }
        return this.getCreatedAt().toLocalDate();
    }

    public int getArticleNum() {
        return this.koreatechArticle.getPortalNum();
    }

    public String getUrl() {
        if (this.koreatechArticle != null) {
            return this.koreatechArticle.getUrl();
        }
        return null;
    }

    public void delete() {
        this.isDeleted = true;
        if (this.koinArticle != null) {
            this.koinArticle.delete();
        }
        if (this.koreatechArticle != null) {
            this.koreatechArticle.delete();
        }
        if (this.lostItemArticle != null) {
            this.lostItemArticle.delete();
        }
        if (this.koinNotice != null) {
            this.koinNotice.delete();
        }
    }

    public void updateKoinNoticeArticle(String title, String content) {
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
        KoinArticle koinArticle,
        LostItemArticle lostItemArticle,
        KoinNotice koinNotice
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
        this.lostItemArticle = lostItemArticle;
        this.koinNotice = koinNotice;
    }

    public static Article createKoinNotice(
        AdminNoticeRequest request,
        Board adminNoticeBoard,
        Admin adminUser
    ) {
        KoinNotice koinNotice = KoinNotice.builder()
            .admin(adminUser)
            .isDeleted(false)
            .build();

        Article article = Article.builder()
            .board(adminNoticeBoard)
            .title(request.title())
            .content(request.content())
            .isNotice(true)
            .koinNotice(koinNotice)
            .isDeleted(false)
            .build();

        koinNotice.setArticle(article);
        return article;
    }

    public static Article createLostItemArticle(
        LostItemArticleRequest request,
        Board lostItemBoard,
        User author
    ) {
        List<LostItemImage> images = request.images().stream()
            .map(image -> LostItemImage.builder()
                .imageUrl(image)
                .isDeleted(false)
                .build())
            .toList();

        LostItemArticle lostItemArticle = LostItemArticle.builder()
            .author(author)
            .type(request.type())
            .category(request.category())
            .foundPlace(getValidatedFoundPlace(request.foundPlace()))
            .foundDate(request.foundDate())
            .images(images)
            .isCouncil(false)
            .isDeleted(false)
            .build();

        if (author.getUserType().equals(COUNCIL)) {
            lostItemArticle.setIsCouncil();
        }

        images.forEach(image -> image.setArticle(lostItemArticle));

        Article article = Article.builder()
            .board(lostItemBoard)
            .title(lostItemArticle.generateTitle())
            .content(request.content())
            .koinArticle(null)
            .koreatechArticle(null)
            .lostItemArticle(lostItemArticle)
            .isDeleted(false)
            .build();

        lostItemArticle.setArticle(article);
        return article;
    }

    private static String getValidatedFoundPlace(String foundPlace) {
        return (foundPlace == null || foundPlace.isBlank()) ? "장소 미상" : foundPlace;
    }
}
