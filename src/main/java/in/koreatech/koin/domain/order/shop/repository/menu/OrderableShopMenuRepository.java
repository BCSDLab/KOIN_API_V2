package in.koreatech.koin.domain.order.shop.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.shop.exception.OrderableShopMenuNotFoundException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;

public interface OrderableShopMenuRepository extends JpaRepository<OrderableShopMenu, Integer> {

    @Query("""
            SELECT DISTINCT osm
            FROM OrderableShopMenu osm
            LEFT JOIN FETCH osm.menuOptionGroupMap
            WHERE osm.id = :orderableShopMenuId
        """)
    Optional<OrderableShopMenu> findByIdWithMenuOptionGroups(@Param("orderableShopMenuId") Integer orderableShopMenuId);

    default OrderableShopMenu getByIdWithMenuOptionGroups(Integer orderableShopMenuId) {
        return findByIdWithMenuOptionGroups(orderableShopMenuId).orElseThrow(
            () -> new OrderableShopMenuNotFoundException(
                "해당 메뉴가 존재하지 않습니다 : " + orderableShopMenuId
            )
        );
    }

    @Query("""
           SELECT osm
           FROM OrderableShopMenu osm
           WHERE osm.orderableShop.id = :orderableShopId
        """)
    Optional<List<OrderableShopMenu>> findAllByOrderableShopId(@Param("orderableShopId") Integer orderableShopId);

    default List<OrderableShopMenu> getAllByOrderableShop(Integer orderableShopId) {
        return findAllByOrderableShopId(orderableShopId).orElseThrow(
            () -> new OrderableShopMenuNotFoundException(
                "해당 상점에 메뉴가 존재하지 않습니다 : " + orderableShopId
            )
        );
    }
}
