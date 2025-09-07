package in.koreatech.koin.domain.order.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.dto.OrderStatusResponse;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusQueryService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderStatusResponse> listLatestActive(Integer userId) {
        var orders = orderRepository.findOrderWithStatus(userId,
            PageRequest.of(0, 1));

        if (orders.isEmpty()) {
            return List.of();
        }

        return orders.stream().map(OrderStatusResponse::from).toList();
    }
}
