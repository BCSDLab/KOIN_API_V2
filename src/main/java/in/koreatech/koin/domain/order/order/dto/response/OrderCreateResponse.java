package in.koreatech.koin.domain.order.order.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.payment.dto.response.BankAccountResponse;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderCreateResponse(
    @Schema(description = "주문 ID", example = "123", requiredMode = REQUIRED)
    Integer orderId,

    @Schema(description = "주문 번호", example = "A1B2C3D4E5", requiredMode = REQUIRED)
    String orderNumber,

    @Schema(description = "결제 ID", example = "456", requiredMode = REQUIRED)
    Integer paymentId,

    @Schema(description = "주문 유형", example = "DELIVERY", requiredMode = REQUIRED)
    String orderType,

    @Schema(description = "주문 상태", example = "CONFIRMING", requiredMode = REQUIRED)
    String orderStatus,

    @Schema(description = "결제 방법", example = "계좌이체", requiredMode = REQUIRED)
    String paymentMethod,

    @Schema(description = "총 결제 금액", example = "18000", requiredMode = REQUIRED)
    Integer amount,

    @Schema(description = "상점 계좌 정보", requiredMode = REQUIRED)
    BankAccountResponse bankAccount
) {
    public static OrderCreateResponse of(Order order, Payment payment) {
        return new OrderCreateResponse(
            order.getId(),
            order.getOrderNumber(),
            payment.getId(),
            order.getOrderType().name(),
            order.getStatus().name(),
            payment.getPaymentMethod().getDisplayName(),
            payment.getAmount(),
            BankAccountResponse.from(payment)
        );
    }
}
