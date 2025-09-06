package in.koreatech.koin.domain.payment.gateway.pg.dto;

public record PaymentGatewayConfirmResponse(
    String paymentKey,
    Integer totalAmount,
    String orderName,
    String orderId,
    String status,
    String method,
    String provider,
    String requestedAt,
    String approvedAt,
    String receipt
) {

}
