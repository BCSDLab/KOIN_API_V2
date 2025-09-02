package in.koreatech.koin.domain.payment.gateway.toss;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.PgOrderIdGenerator;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;
import in.koreatech.koin.domain.payment.gateway.toss.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentGatewayService implements PaymentGatewayService {

    private final TossPaymentClient tossPaymentClient;
    private final PgOrderIdGenerator pgOrderIdGenerator;

    public PaymentGatewayConfirmResponse confirmPayment(String paymentKey, String pgOrderId, Integer amount) {
        TossPaymentConfirmResponse tossPaymentConfirmResponse = tossPaymentClient.requestConfirm(paymentKey, pgOrderId,
            amount);

        return new PaymentGatewayConfirmResponse(
            tossPaymentConfirmResponse.paymentKey(),
            tossPaymentConfirmResponse.totalAmount(),
            tossPaymentConfirmResponse.orderId(),
            tossPaymentConfirmResponse.status(),
            tossPaymentConfirmResponse.method(),
            tossPaymentConfirmResponse.requestedAt(),
            tossPaymentConfirmResponse.approvedAt()
        );
    }

    public String generatePgOrderId() {
        return pgOrderIdGenerator.generatePgOrderId();
    }
}
