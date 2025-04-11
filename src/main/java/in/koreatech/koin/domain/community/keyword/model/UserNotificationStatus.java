package in.koreatech.koin.domain.community.keyword.model;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_notification_status", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "last_notified_article_id", nullable = false)
    private Integer lastNotifiedArticleId;

    @Builder
    public UserNotificationStatus(Integer userId, Integer lastNotifiedArticleId) {
        this.userId = userId;
        this.lastNotifiedArticleId = lastNotifiedArticleId;
    }

    public void updateLastNotifiedArticleId(Integer articleId) {
        this.lastNotifiedArticleId = articleId;
    }
}
