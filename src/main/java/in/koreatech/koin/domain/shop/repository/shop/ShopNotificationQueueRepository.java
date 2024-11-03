package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.shop.ShopNotificationQueue;

public interface ShopNotificationQueueRepository extends Repository<ShopNotificationQueue, Integer> {

    ShopNotificationQueue save(ShopNotificationQueue shopNotificationQueue);

    List<ShopNotificationQueue> findByNotificationTimeBefore(LocalDateTime now);

    void deleteAll(List<ShopNotificationQueue> shopNotificationQueues);
}
