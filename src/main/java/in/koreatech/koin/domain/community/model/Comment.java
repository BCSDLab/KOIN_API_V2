package in.koreatech.koin.domain.community.model;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "article_id", nullable = false)
    private Long articleId;

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
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Transient
    private Boolean grantEdit = false;

    @Transient
    private Boolean grantDelete = false;

    public void updateAuthority(Long userId) {
        if (this.userId.equals(userId)) {
            this.grantEdit = true;
            this.grantDelete = true;
        }
    }

    public Boolean getGrantEdit() {
        return grantEdit;
    }

    public Boolean getGrantDelete() {
        return grantDelete;
    }

    @Builder
    public Comment(Long id, Long articleId, String content, Long userId, String nickname, Boolean isDeleted) {
        this.id = id;
        this.articleId = articleId;
        this.content = content;
        this.userId = userId;
        this.nickname = nickname;
        this.isDeleted = isDeleted;
    }
}
