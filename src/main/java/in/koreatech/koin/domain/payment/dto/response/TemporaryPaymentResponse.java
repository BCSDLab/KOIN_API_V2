package in.koreatech.koin.domain.payment.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TemporaryPaymentResponse(
    @Schema(description = "주문 번호", example = "a4CWyWY5m89PNh7xJwhk1", requiredMode = REQUIRED)
    String orderId
) {
    public static TemporaryPaymentResponse of(String orderId) {
        return new TemporaryPaymentResponse(orderId);
    }
}
