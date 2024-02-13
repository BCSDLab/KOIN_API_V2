package in.koreatech.koin.domain.community.model;

import java.util.List;

import org.hibernate.annotations.Where;
import org.jsoup.Jsoup;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "articles")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    private static final int SUMMARY_MIN_LENGTH = 0;
    private static final int SUMMARY_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @NotNull
    @Column(name = "hit", nullable = false)
    private Long hit;

    @Size(max = 45)
    @NotNull
    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @NotNull
    @Column(name = "is_solved", nullable = false)
    private Boolean isSolved = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Comment> comment;

    @NotNull
    @Column(name = "comment_count", nullable = false)
    private Byte commentCount;

    @Lob
    @Column(name = "meta")
    private String meta;

    @NotNull
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice = false;

    @Column(name = "notice_article_id")
    private Long noticeArticleId;

    @Transient
    private String summary;

    @Transient
    private String contentSummary;

    @PostLoad
    public void updateContentSummary() {
        if (content == null) {
            contentSummary = "";
            return;
        }
        String parseResult = Jsoup.parse(content).text().replace("&nbsp", "").strip();
        if (parseResult.length() < SUMMARY_MAX_LENGTH) {
            contentSummary = parseResult;
            return;
        }
        contentSummary = parseResult.substring(SUMMARY_MIN_LENGTH, SUMMARY_MAX_LENGTH);
    }

    public void increaseHit() {
        hit++;
    }

    @Builder
    private Article(Board board, String title, String content, User user, String nickname, Long hit,
        String ip, Boolean isSolved, Boolean isDeleted, Byte commentCount, String meta, Boolean isNotice,
        Long noticeArticleId) {
        this.board = board;
        this.title = title;
        this.content = content;
        this.user = user;
        this.nickname = nickname;
        this.hit = hit;
        this.ip = ip;
        this.isSolved = isSolved;
        this.isDeleted = isDeleted;
        this.commentCount = commentCount;
        this.meta = meta;
        this.isNotice = isNotice;
        this.noticeArticleId = noticeArticleId;
    }
}
