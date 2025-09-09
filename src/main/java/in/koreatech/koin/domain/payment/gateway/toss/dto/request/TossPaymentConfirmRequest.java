package in.koreatech.koin.domain.payment.gateway.toss.dto.request;

public record TossPaymentConfirmRequest(
    String paymentKey,
    String orderId,
    Integer amount
) {

}
