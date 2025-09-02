package in.koreatech.koin.domain.payment.gateway.pg;

import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayCancelResponse;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;

public interface PaymentGatewayService {
    PaymentGatewayConfirmResponse confirmPayment(String paymentKey, String pgOrderId, Integer amount);
    PaymentGatewayCancelResponse cancelPayment(String paymentKey, String cancelReason, String idempotencyKey);
    String generatePgOrderId();
}
