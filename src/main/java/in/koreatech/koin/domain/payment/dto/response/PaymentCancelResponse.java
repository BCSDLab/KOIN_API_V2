package in.koreatech.koin.domain.payment.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.model.PaymentCancel;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record PaymentCancelResponse(
    @Schema(description = "결제 취소 내역", requiredMode = REQUIRED)
    List<InnerPaymentCancelResponse> paymentCancels
) {
    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerPaymentCancelResponse(
        @Schema(description = "결제 취소 고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "결제 취소 이유", example = "단숨 변심", requiredMode = REQUIRED)
        String cancelReason,

        @Schema(description = "결제 취소 금액", example = "1000", requiredMode = REQUIRED)
        Integer cancelAmount,

        @Schema(description = "결제 취소 일시", example = "2025.06.21 21:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime canceledAt
    ) {

    }

    public static PaymentCancelResponse from(List<PaymentCancel> paymentCancels) {
        List<InnerPaymentCancelResponse> responses = new ArrayList<>();

        for (PaymentCancel paymentCancel : paymentCancels) {
            responses.add(new InnerPaymentCancelResponse(
                paymentCancel.getId(),
                paymentCancel.getCancelReason(),
                paymentCancel.getCancelAmount(),
                paymentCancel.getCanceledAt()
            ));
        }

        return new PaymentCancelResponse(responses);
    }
}
