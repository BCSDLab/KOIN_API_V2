package in.koreatech.koin.domain.order.order.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.order.dto.request.OrderSearchCondition;
import in.koreatech.koin.domain.order.order.dto.response.InprogressOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderInfo;
import in.koreatech.koin.domain.order.order.model.OrderSearchCriteria;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.order.repository.OrderSearchQueryRepository;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderSearchQueryRepository orderSearchQueryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public OrdersResponse getOrders(Integer userId, OrderSearchCondition orderSearchCondition) {
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.from(orderSearchCondition);
        Page<OrderInfo> orderPage = orderSearchQueryRepository.findOrdersByCondition(userId, orderSearchCriteria);
        return OrdersResponse.of(orderPage);
    }

    public List<InprogressOrderResponse> getInprogressOrders(Integer userId) {
        User user = userRepository.getById(userId);
        List<Order> orders = orderRepository.findOrderWithStatus(user.getId());

        return orders.stream()
            .map(order -> {
                Payment payment = paymentRepository.getByOrderId(order.getId());
                return InprogressOrderResponse.from(order, payment);
            })
            .toList();
    }
}
