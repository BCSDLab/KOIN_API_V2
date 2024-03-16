package in.koreatech.koin.domain.community.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_view_logs")
@NoArgsConstructor(access = PROTECTED)
public class ArticleViewLog {

    private static final Long EXPIRED_HOUR = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "expired_at", nullable = false)
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
