package in.koreatech.koin.domain.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.dto.OrderPreparingResponse;
import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPreparingQueryService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<OrderPreparingResponse> getInprogressOrders(Integer userId) {
        User user = userRepository.getById(userId);
        List<Order> orders = orderRepository.findOrderWithStatus(user.getId());

        return orders.stream()
            .map(order -> {
                Payment payment = paymentRepository.getByOrderId(order.getId());
                return OrderPreparingResponse.from(order, payment);
            })
            .toList();
    }
}
