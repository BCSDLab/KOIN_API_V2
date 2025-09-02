package in.koreatech.koin.domain.payment.gateway.toss;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.PgOrderIdGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentGatewayService implements PaymentGatewayService {

    private final PgOrderIdGenerator pgOrderIdGenerator;

    public String generatePgOrderId() {
        return pgOrderIdGenerator.generatePgOrderId();
    }
}
