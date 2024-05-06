package in.koreatech.koin.global.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.global.domain.notification.exception.NotificationSubscribeNotFoundException;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;

public interface NotificationDetailSubscribeRepository extends Repository<NotificationDetailSubscribe, Integer> {

    NotificationDetailSubscribe save(NotificationDetailSubscribe notificationDetailSubscribe);

    List<NotificationDetailSubscribe> findAllBySubscribeType(NotificationSubscribeType subscribeType);

    List<NotificationDetailSubscribe> findAllByUserId(Integer userId);

    List<NotificationDetailSubscribe> findAllByDetailSubscribeType(DiningType diningType);

    Optional<NotificationDetailSubscribe> findByUserIdAndDetailSubscribeType(Integer userId, String detailType);

    void deleteByUserIdAndDetailSubscribeType(Integer userId, NotificationDetailSubscribeType type);

}
