package in.koreatech.koin.domain.community.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_view_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleViewLog {

    private static final Long EXPIRED_HOUR = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "user_id")
    private Long userId;

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
    public ArticleViewLog(Long articleId, Long userId, String ip) {
        this.articleId = articleId;
        this.userId = userId;
        this.ip = ip;
        updateExpiredTime();
    }
}
