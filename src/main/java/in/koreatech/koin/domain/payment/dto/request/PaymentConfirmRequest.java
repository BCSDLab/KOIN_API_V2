package in.koreatech.koin.domain.payment.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record PaymentConfirmRequest(
    @Schema(description = "결제 키", example = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1", requiredMode = REQUIRED)
    @NotBlank(message = "결제 키값은 필수 입력사항입니다.")
    String paymentKey,

    @Schema(description = "주문 번호", example = "a4CWyWY5m89PNh7xJwhk1", requiredMode = REQUIRED)
    @NotBlank(message = "주문 번호는 필수 입력사항입니다.")
    String orderId,

    @Schema(description = "결제 금액", example = "1000", requiredMode = REQUIRED)
    @NotNull(message = "결제 금액은 필수 입력사항입니다.")
    Integer amount
) {

}
