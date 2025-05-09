package in.koreatech.koin.domain.notification.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notification_subscribe", uniqueConstraints = {
    @UniqueConstraint(
        name = "unique_user_id_subscribe_type_detail_type",
        columnNames = {"user_id", "subscribe_type", "detail_type"}
    )
})
@NoArgsConstructor(access = PROTECTED)
public class NotificationSubscribe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(name = "subscribe_type", nullable = false)
    private NotificationSubscribeType subscribeType;

    @Enumerated(STRING)
    @Column(name = "detail_type")
    private NotificationDetailSubscribeType detailType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private NotificationSubscribe(
        NotificationSubscribeType subscribeType,
        NotificationDetailSubscribeType detailType,
        User user
    ) {
        this.subscribeType = subscribeType;
        this.detailType = detailType;
        this.user = user;
    }
}
