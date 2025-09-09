package in.koreatech.koin.domain.payment.model.domain;

public record PaymentCancelInfo(
    String cancelReason
) {
    public static PaymentCancelInfo of(String cancelReason) {
        return new PaymentCancelInfo(cancelReason);
    }
}
