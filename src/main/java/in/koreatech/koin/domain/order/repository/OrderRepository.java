package in.koreatech.koin.domain.order.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.model.Order;

public interface OrderRepository extends Repository<Order, String> {

    void save(Order order);
}
