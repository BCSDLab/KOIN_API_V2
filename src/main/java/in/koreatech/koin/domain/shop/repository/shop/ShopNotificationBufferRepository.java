package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.model.shop.ShopNotificationBuffer;

public interface ShopNotificationBufferRepository extends Repository<ShopNotificationBuffer, Integer> {

    ShopNotificationBuffer save(ShopNotificationBuffer shopNotificationBuffer);

    @Query("""
            SELECT b FROM ShopNotificationBuffer b
            JOIN FETCH b.shop s
            JOIN FETCH b.user u
            JOIN FETCH s.shopCategories scm
            JOIN FETCH scm.shopCategory sc
            JOIN FETCH sc.parentCategory smc
            JOIN FETCH smc.notificationMessage m
            WHERE b.notificationTime < :now
                  AND sc.id = (
                      SELECT MIN(sc2.id)
                      FROM ShopCategory sc2
                      JOIN ShopCategoryMap scm2 ON scm2.shopCategory = sc2
                      WHERE scm2.shop = s
                      AND sc2.name != '전체보기'
                  )
        """)
    List<ShopNotificationBuffer> findByNotificationTimeBefore(@Param("now") LocalDateTime now);

    List<ShopNotificationBuffer> findAll();

    int deleteByNotificationTimeBefore(LocalDateTime now);
}
