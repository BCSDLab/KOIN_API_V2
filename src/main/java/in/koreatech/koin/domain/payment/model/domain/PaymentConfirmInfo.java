package in.koreatech.koin.domain.payment.model.domain;

public record PaymentConfirmInfo(
    String paymentKey,
    String orderId,
    Integer amount
) {
    public static PaymentConfirmInfo of(String paymentKey, String orderId, Integer amount) {
        return new PaymentConfirmInfo(paymentKey, orderId, amount);
    }
}
