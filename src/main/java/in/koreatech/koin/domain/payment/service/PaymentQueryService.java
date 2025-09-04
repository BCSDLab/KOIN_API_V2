package in.koreatech.koin.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.payment.dto.response.PaymentResponse;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse getPayment(User user, Integer paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        payment.validateUserIdMatches(user.getId());
        Order order = payment.getOrder();

        return PaymentResponse.of(payment, order);
    }
}
