package in.koreatech.koin.domain.payment.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record PaymentCancelRequest(
    @Schema(description = "결제 취소 사유", example = "잘못 주문했어요.", requiredMode = REQUIRED)
    @NotBlank(message = "결제 취소 사유는 필수 입력값입니다.")
    String cancelReason
) {

}
