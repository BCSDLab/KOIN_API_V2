package in.koreatech.koin.domain.order.shop.repository.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.global.marker.JpaRepositoryMarker;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenuOptions;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;

@JpaRepositoryMarker
public interface OrderableShopMenuOptionRepository extends JpaRepository<OrderableShopMenuOption, Integer> {

    @Query("SELECT mo FROM OrderableShopMenuOption mo " +
        "JOIN mo.optionGroup og " +
        "JOIN OrderableShopMenuOptionGroupMap mogm ON mogm.optionGroup = og " +
        "WHERE mogm.menu.id = :menuId")
    List<OrderableShopMenuOption> findAllOrderableShopMenuOptionByMenuId(@Param("menuId") Integer menuId);

    default OrderableShopMenuOptions getAllByMenuId(Integer menuId) {
        List<OrderableShopMenuOption> allOrderableShopMenuOptionByMenuId = findAllOrderableShopMenuOptionByMenuId(
            menuId);
        return new OrderableShopMenuOptions(allOrderableShopMenuOptionByMenuId);
    }
}
