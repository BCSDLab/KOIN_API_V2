package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.model.shop.ShopNotificationQueue;

public interface ShopNotificationQueueRepository extends Repository<ShopNotificationQueue, Integer> {

    ShopNotificationQueue save(ShopNotificationQueue shopNotificationQueue);

    @Query("""
            SELECT q FROM ShopNotificationQueue q
            JOIN FETCH q.shop s
            JOIN FETCH q.user u
            JOIN FETCH s.shopCategories scm
            JOIN FETCH scm.shopCategory sc
            JOIN FETCH sc.mainCategory smc
            JOIN FETCH smc.notificationMessage m
            WHERE q.notificationTime < :now
                  AND sc.id = (
                      SELECT MIN(sc2.id)
                      FROM ShopCategory sc2
                      JOIN ShopCategoryMap scm2 ON scm2.shopCategory = sc2
                      WHERE scm2.shop = s
                      AND sc2.name != '전체보기'
                  )
        """)
    List<ShopNotificationQueue> findByNotificationTimeBefore(@Param("now") LocalDateTime now);

    List<ShopNotificationQueue> findAll();

    int deleteByNotificationTimeBefore(LocalDateTime now);
}
