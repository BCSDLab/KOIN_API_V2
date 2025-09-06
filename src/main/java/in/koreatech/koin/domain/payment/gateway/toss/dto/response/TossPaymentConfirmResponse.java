package in.koreatech.koin.domain.payment.gateway.toss.dto.response;

public record TossPaymentConfirmResponse(
    String paymentKey,
    String type,
    String orderId,
    String orderName,
    String mId,
    String currency,
    String method,
    Integer totalAmount,
    Integer balanceAmount,
    String status,
    String requestedAt,
    String approvedAt,
    Boolean useEscrow,
    String lastTransactionKey,
    Integer suppliedAmount,
    Integer vat,
    Boolean cultureExpense,
    Integer taxFreeAmount,
    Integer taxExemptionAmount,
    Boolean isPartialCancelable,
    Card card,
    EasyPay easyPay,
    String country
) {
    public record Card(
        Integer amount,
        String issuerCode,
        String acquirerCode,
        String number,
        Integer installmentPlanMonths,
        String approveNo,
        Boolean useCardPoint,
        String cardType,
        String ownerType,
        String acquireStatus,
        Boolean isInterestFree,
        String interestPayer
    ) {

    }

    public record EasyPay(
        String provider,
        Integer amount,
        Integer discountAmount
    ) {

    }
}
