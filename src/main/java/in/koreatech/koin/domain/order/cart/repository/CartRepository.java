package in.koreatech.koin.domain.order.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.cart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query(value = "SELECT c.* " +
        "FROM cart c " +
        "LEFT JOIN cart_menu_item cmi ON c.id = cmi.cart_id " +
        "WHERE c.user_id = :userId " +
        "AND c.orderable_shop_id = :orderableShopId",
        nativeQuery = true)
    Optional<Cart> findCartByUserAndShop(
        @Param("userId") Integer userId,
        @Param("orderableShopId") Integer orderableShopId
    );

    @Query(value = "SELECT c.* " +
        "FROM cart c " +
        "LEFT JOIN cart_menu_item cmi ON c.id = cmi.cart_id " +
        "WHERE c.user_id = :userId ",
        nativeQuery = true)
    Optional<Cart> findCartByUserId(
        @Param("userId") Integer userId
    );

    void deleteByUserId(Integer userId);
}
