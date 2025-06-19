package in.koreatech.koin.domain.order.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.cart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query("SELECT c FROM Cart c " +
        "LEFT JOIN FETCH c.orderableShop os " +
        "LEFT JOIN FETCH c.cartMenuItems cmi " +
        "WHERE c.user.id = :userId")
    Optional<Cart> findCartByUserId(@Param("userId") Integer userId);

    void deleteByUserId(Integer userId);
}
