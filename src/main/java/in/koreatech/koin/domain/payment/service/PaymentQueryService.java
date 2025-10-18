package in.koreatech.koin.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderTakeout;
import in.koreatech.koin.domain.payment.dto.response.PaymentResponse;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;
import static in.koreatech.koin.domain.order.order.model.OrderType.TAKE_OUT;
import static in.koreatech.koin.domain.order.order.model.OrderStatus.CONFIRMING;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse getPayment(User user, Integer paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        payment.validateUserIdMatches(user.getId());
        Order order = payment.getOrder();

        LocalTime estimatedAt = computeEstimatedTime(order).orElse(null);

        return PaymentResponse.of(payment, order, estimatedAt);
    }

    private Optional<LocalTime> computeEstimatedTime(Order order) {
        if (order.getStatus() == CONFIRMING) {
            return Optional.empty();
        }
        if (order.getOrderType() == DELIVERY) {
            OrderDelivery delivery = order.getOrderDelivery();
            if (delivery != null && delivery.getEstimatedArrivalAt() != null) {
                return Optional.of(LocalTime.from(delivery.getEstimatedArrivalAt()));
            }
        } else if (order.getOrderType() == TAKE_OUT) {
            OrderTakeout takeout = order.getOrderTakeout();
            if (takeout != null && takeout.getEstimatedPackagedAt() != null) {
                return Optional.of(LocalTime.from(takeout.getEstimatedPackagedAt()));
            }
        }
        return Optional.empty();
    }
}
