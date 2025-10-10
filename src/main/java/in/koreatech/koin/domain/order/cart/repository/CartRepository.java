package in.koreatech.koin.domain.order.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.global.marker.JpaRepositoryMarker;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@JpaRepositoryMarker
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query("""
        SELECT c FROM Cart c
        LEFT JOIN FETCH c.orderableShop
        LEFT JOIN FETCH c.orderableShop.deliveryOption
        LEFT JOIN FETCH c.cartMenuItems
        WHERE c.user.id = :userId
    """)
    Optional<Cart> findCartByUserId(@Param("userId") Integer userId);

    default Cart getCartByUserId(Integer userId) {
        return findCartByUserId(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CART));
    }

    void deleteByUserId(Integer userId);
}
