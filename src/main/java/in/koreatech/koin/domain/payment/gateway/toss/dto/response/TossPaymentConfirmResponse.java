package in.koreatech.koin.domain.payment.gateway.toss.dto.response;

public record TossPaymentConfirmResponse(
    String paymentKey,
    Integer totalAmount,
    String orderId,
    String status,
    String method,
    String requestedAt,
    String approvedAt
) {

}
