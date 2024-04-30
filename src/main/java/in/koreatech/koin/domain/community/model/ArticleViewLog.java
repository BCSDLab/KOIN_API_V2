package in.koreatech.koin.domain.community.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_view_logs", uniqueConstraints = {
    @UniqueConstraint(
        name = "article_view_logs_article_id_user_id_unique",
        columnNames = {"article_id", "user_id"}
    )
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleViewLog {

    private static final Long EXPIRED_HOUR = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "expired_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime expiredAt = LocalDateTime.now().plusHours(EXPIRED_HOUR);

    @Size(max = 45)
    @NotNull
    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    public void updateExpiredTime() {
        expiredAt = LocalDateTime.now().plusHours(EXPIRED_HOUR);
    }

    @Builder
    private ArticleViewLog(Article article, User user, String ip) {
        this.article = article;
        this.user = user;
        this.ip = ip;
        updateExpiredTime();
    }
}
