package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.domain.order.order.model.OrderStatus.*;
import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_ORDER;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.model.OrderTakeout;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
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

        if (orderStatus != CONFIRMING) {
            if (order.getOrderType().equals(TAKE_OUT)) {
                if (order.getOrderTakeout().getEstimatedPackagedAt() == null) {
                    OrderTakeout orderTakeout = order.getOrderTakeout();
                    orderTakeout.updateEstimatedPackagedAt();
                }
            }
            else if (order.getOrderType().equals(DELIVERY)) {
                if (order.getOrderDelivery().getEstimatedArrivalAt() == null) {
                    OrderDelivery orderDelivery = order.getOrderDelivery();
                    orderDelivery.updateEstimatedArrivalAt();
                }
            }
        }

        if (orderStatus.equals(COOKING)) {
            if (order.getOrderType().equals(TAKE_OUT)) {
                OrderTakeout orderTakeout = order.getOrderTakeout();
                orderTakeout.cooking();
            } else if (order.getOrderType().equals(DELIVERY)) {
                OrderDelivery orderDelivery = order.getOrderDelivery();
                orderDelivery.cooking();
            }
        } else if (orderStatus.equals(PACKAGED) && order.getOrderType().equals(TAKE_OUT)) {
            OrderTakeout orderTakeout = order.getOrderTakeout();
            orderTakeout.packaged();
        } else if (orderStatus.equals(DELIVERED) && order.getOrderType().equals(DELIVERY)) {
            OrderDelivery orderDelivery = order.getOrderDelivery();
            orderDelivery.delivered();
        } else if (orderStatus.equals(PICKED_UP) && order.getOrderType().equals(TAKE_OUT)) {
            OrderTakeout orderTakeout = order.getOrderTakeout();
            orderTakeout.pickedUp();
        } else if (orderStatus.equals(DELIVERING) && order.getOrderType().equals(DELIVERY)) {
            OrderDelivery orderDelivery = order.getOrderDelivery();
            orderDelivery.delivering();
        }
    }
}
