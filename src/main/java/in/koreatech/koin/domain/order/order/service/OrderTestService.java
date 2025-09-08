package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.domain.order.model.OrderStatus.DELIVERED;
import static in.koreatech.koin.domain.order.model.OrderStatus.PACKAGED;
import static in.koreatech.koin.domain.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_ORDER;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.model.OrderStatus;
import in.koreatech.koin.domain.order.model.OrderTakeout;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class OrderTestService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateOrderStatus(Integer orderId, Integer userId, OrderStatus orderStatus) {
        User user = userRepository.getById(userId);
        Order order = orderRepository.getById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw CustomException.of(FORBIDDEN_ORDER);
        }

        if (orderStatus.equals(DELIVERED) && order.getOrderType().equals(DELIVERY)) {
            OrderDelivery orderDelivery = order.getOrderDelivery();
            orderDelivery.deliveryComplete();
        } else if (orderStatus.equals(PACKAGED) && order.getOrderType().equals(TAKE_OUT)) {
            OrderTakeout orderTakeout = order.getOrderTakeout();
            orderTakeout.takeoutComplete();
        }
    }
}
