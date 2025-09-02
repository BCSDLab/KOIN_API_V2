package in.koreatech.koin.domain.order.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.model.OrderMenu;

public interface OrderMenuRepository extends Repository<OrderMenu, Integer> {

    void saveAll(Iterable<OrderMenu> orderMenus);

    List<OrderMenu> findAllByOrderId(Integer orderId);
}
