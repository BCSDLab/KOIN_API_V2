package in.koreatech.koin.domain.order.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.global.exception.CustomException;

public interface OrderRepository extends Repository<Order, Integer> {

    void save(Order order);

    Optional<Order> findById(Integer orderId);

    default Order getById(Integer orderId) {
        return findById(orderId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_ORDER));
    }
}
