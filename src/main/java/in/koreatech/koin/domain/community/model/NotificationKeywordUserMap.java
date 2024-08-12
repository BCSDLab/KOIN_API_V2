package in.koreatech.koin.domain.community.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notification_keyword_user_map", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"keyword_id", "user_id"})
})
@NoArgsConstructor(access = PROTECTED)
public class NotificationKeywordUserMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "keyword_id", nullable = false)
    private NotificationKeyword notificationKeyword;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
