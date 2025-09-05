package in.koreatech.koin.domain.payment.service;

import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_ALREADY_CANCELED;
import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_CANCEL_ERROR;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.model.entity.PaymentCancel;
import in.koreatech.koin.domain.payment.model.entity.PaymentStatus;
import in.koreatech.koin.domain.payment.dto.response.PaymentCancelResponse;
import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayCancelResponse;
import in.koreatech.koin.domain.payment.mapper.PaymentCancelMapper;
import in.koreatech.koin.domain.payment.model.domain.PaymentCancelInfo;
import in.koreatech.koin.domain.payment.repository.PaymentCancelRepository;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final PaymentRepository paymentRepository;
    private final PaymentCancelRepository paymentCancelRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentIdempotencyKeyService paymentIdempotencyKeyService;
    private final PaymentCancelMapper paymentCancelMapper;

    @Transactional
    public PaymentCancelResponse cancelPayment(User user, Integer paymentId, PaymentCancelInfo paymentCancelInfo) {
        Payment payment = paymentRepository.getById(paymentId);
        validatePaymentStatusIsNotCanceled(payment);
        payment.validateUserIdMatches(user.getId());

        String paymentIdempotencyKey = paymentIdempotencyKeyService.getOrCreate(user.getId());
        PaymentGatewayCancelResponse pgResponse = paymentGatewayService.cancelPayment(payment.getPaymentKey(),
            paymentCancelInfo.cancelReason(), paymentIdempotencyKey);
        validatePaymentIsCanceled(pgResponse.status());

        payment.cancel();
        List<PaymentCancel> paymentCancels = paymentCancelMapper.toEntity(payment, pgResponse);
        paymentCancelRepository.saveAll(paymentCancels);

        return PaymentCancelResponse.from(paymentCancels);
    }

    private void validatePaymentStatusIsNotCanceled(Payment payment) {
        if (payment.getPaymentStatus().isCanceled()) {
            throw CustomException.of(PAYMENT_ALREADY_CANCELED);
        }
    }

    private void validatePaymentIsCanceled(String status) {
        if (!PaymentStatus.valueOf(status).isCanceled()) {
            throw CustomException.of(PAYMENT_CANCEL_ERROR);
        }
    }
}
