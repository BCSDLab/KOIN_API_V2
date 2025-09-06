package in.koreatech.koin.domain.payment.gateway.toss;

import static in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayCancelResponse.CancelInfo;
import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_CONFIRM_ERROR;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.PgOrderIdGenerator;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayCancelResponse;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;
import in.koreatech.koin.domain.payment.gateway.toss.dto.response.TossPaymentCancelResponse;
import in.koreatech.koin.domain.payment.gateway.toss.dto.response.TossPaymentConfirmResponse;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentGatewayService implements PaymentGatewayService {

    private final TossPaymentClient tossPaymentClient;
    private final PgOrderIdGenerator pgOrderIdGenerator;
    private final ObjectMapper objectMapper;

    public PaymentGatewayConfirmResponse confirmPayment(String paymentKey, String pgOrderId, Integer amount) {
        try {
            TossPaymentConfirmResponse tossPaymentConfirmResponse = tossPaymentClient.requestConfirm(paymentKey, pgOrderId,
                amount);

            return new PaymentGatewayConfirmResponse(
                tossPaymentConfirmResponse.paymentKey(),
                tossPaymentConfirmResponse.totalAmount(),
                tossPaymentConfirmResponse.orderName(),
                tossPaymentConfirmResponse.orderId(),
                tossPaymentConfirmResponse.status(),
                tossPaymentConfirmResponse.method(),
                tossPaymentConfirmResponse.easyPay().provider(),
                tossPaymentConfirmResponse.requestedAt(),
                tossPaymentConfirmResponse.approvedAt(),
                objectMapper.writeValueAsString(tossPaymentConfirmResponse)
            );
        } catch (JsonProcessingException e) {
            // TODO. 결제 롤백 로직 구현
            throw CustomException.of(PAYMENT_CONFIRM_ERROR);
        }
    }

    public PaymentGatewayCancelResponse cancelPayment(String paymentKey, String cancelReason, String idempotencyKey) {
        TossPaymentCancelResponse tossPaymentCancelResponse = tossPaymentClient.requestCancel(paymentKey, cancelReason, idempotencyKey);

        return new PaymentGatewayCancelResponse(
            tossPaymentCancelResponse.paymentKey(),
            tossPaymentCancelResponse.orderId(),
            tossPaymentCancelResponse.status(),
            tossPaymentCancelResponse.cancels().stream()
                .map(cancelInfo -> new CancelInfo(
                    cancelInfo.cancelAmount(),
                    cancelInfo.cancelReason(),
                    cancelInfo.canceledAt(),
                    cancelInfo.transactionKey()
                ))
                .toList()
        );
    }

    public String generatePgOrderId() {
        return pgOrderIdGenerator.generatePgOrderId();
    }
}
