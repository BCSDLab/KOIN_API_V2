package in.koreatech.koin.domain.payment.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.payment.model.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BankAccountResponse(
    @Schema(description = "은행명", example = "국민은행", requiredMode = REQUIRED)
    String bank,

    @Schema(description = "계좌번호", example = "123456-78-901234", requiredMode = REQUIRED)
    String accountNumber,

    @Schema(description = "예금주", example = "홍길동", requiredMode = REQUIRED)
    String accountHolder
) {
    public static BankAccountResponse from(Payment payment) {
        if (!payment.isDirectBankTransfer()) {
            return null;
        }
        return new BankAccountResponse(payment.getBank(), payment.getAccountNumber(), payment.getAccountHolder());
    }
}
