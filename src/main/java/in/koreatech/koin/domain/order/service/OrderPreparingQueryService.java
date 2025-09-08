package in.koreatech.koin.domain.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.dto.OrderPreparingResponse;
import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPreparingQueryService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<OrderPreparingResponse> getListOrderPreparing(Integer userId) {
        List<Order> orders = orderRepository.findOrderWithStatus(userId);

        return orders.stream()
            .map(order -> {
                String desc = paymentRepository.getDescriptionByOrderId(order.getId());
                return OrderPreparingResponse.from(order, desc);
            })
            .toList();
    }
}
