package in.koreatech.koin.domain.community.model;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    @Builder
    private Article(Long boardId, String title, String content, Long userId, String nickname, Long hit,
        String ip, Boolean isSolved, Boolean isDeleted, Byte commentCount, String meta, Boolean isNotice,
        Long noticeArticleId) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.userId = userId;
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
