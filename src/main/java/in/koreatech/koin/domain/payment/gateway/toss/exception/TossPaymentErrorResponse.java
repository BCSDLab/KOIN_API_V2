package in.koreatech.koin.domain.payment.gateway.toss.exception;

public record TossPaymentErrorResponse(
    String code,
    String message
) {
    
}
