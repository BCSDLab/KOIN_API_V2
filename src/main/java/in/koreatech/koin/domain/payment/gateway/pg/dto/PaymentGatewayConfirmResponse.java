package in.koreatech.koin.domain.payment.gateway.pg.dto;

public record PaymentGatewayConfirmResponse(
    String paymentKey,
    Integer totalAmount,
    String orderId,
    String status,
    String method,
    String requestedAt,
    String approvedAt
) {

}
