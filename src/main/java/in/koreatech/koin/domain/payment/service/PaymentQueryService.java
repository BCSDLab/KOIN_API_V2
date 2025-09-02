package in.koreatech.koin.domain.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderMenu;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.order.repository.OrderMenuRepository;
import in.koreatech.koin.domain.payment.dto.response.PaymentResponse;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final OrderMenuRepository orderMenuRepository;

    public PaymentResponse getPayment(User user, Integer paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        payment.validateUserIdMatches(user.getId());

        Order order = payment.getOrder();
        List<OrderMenu> orderMenus = orderMenuRepository.findAllByOrderId(order.getId());

        return PaymentResponse.of(payment, order, orderMenus);
    }
}
