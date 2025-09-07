package in.koreatech.koin.domain.order.order.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.model.OrderInfo;
import in.koreatech.koin.domain.order.order.dto.request.OrderSearchCondition;
import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.domain.order.repository.OrderSearchQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderSearchQueryRepository orderSearchQueryRepository;

    public OrdersResponse getOrders(Integer userId, OrderSearchCondition orderSearchCondition) {
        Page<OrderInfo> orderPage = orderSearchQueryRepository.findOrdersByCondition(userId, orderSearchCondition);
        return OrdersResponse.of(orderPage);
    }
}
