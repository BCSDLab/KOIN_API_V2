package in.koreatech.koin.domain.payment.gateway.pg.dto;

import java.util.List;

public record PaymentGatewayCancelResponse(
    String paymentKey,
    String orderId,
    String status,
    List<CancelInfo> cancels
) {
    public record CancelInfo(
        Integer cancelAmount,
        String cancelReason,
        String canceledAt,
        String transactionKey
    ) {

    }
}
