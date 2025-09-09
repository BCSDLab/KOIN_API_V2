package in.koreatech.koin.domain.payment.gateway.toss.dto.response;

import java.util.List;

public record TossPaymentCancelResponse(
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
