package in.koreatech.koin.domain.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.exception.NotificationMessageNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;

public interface ShopCategoryMapRepository extends Repository<ShopCategoryMap, Integer> {

    ShopCategoryMap save(ShopCategoryMap shopCategoryMap);

    List<ShopCategoryMap> findAllByShopId(Integer shopId);

    @Query("""
            SELECT smc.notificationMessage FROM ShopCategoryMap scm
            JOIN scm.shopCategory sc
            JOIN sc.mainCategory smc
            WHERE scm.shop.id = :shopId AND sc.name != '전체보기'
        """)
    Optional<ShopNotificationMessage> findNotificationMessageByShopId(@Param("shopId") Integer shopId);

    default ShopNotificationMessage getNotificationMessageByShopId(Integer shopId) {
        return findNotificationMessageByShopId(shopId)
            .orElseThrow(() -> NotificationMessageNotFoundException.withDetail("shopId: " + shopId));
    }
}
