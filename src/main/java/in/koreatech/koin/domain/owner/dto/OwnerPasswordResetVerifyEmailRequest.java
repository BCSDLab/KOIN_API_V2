package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerPasswordResetVerifyEmailRequest(
    @JsonProperty(value = "address")
    @NotBlank(message = "검증값은 필수입니다.")
    @Schema(description = "검증값 (전화번호, 이메일)", example = "01012341234", requiredMode = REQUIRED)
    String address,

    @NotBlank(message = "인증 코드는 필수입니다.")
    @Digits(integer = 6, fraction = 0, message = "인증 코드는 6자리 정수여야 합니다. ${validatedValue}")
    @Schema(description = "인증 코드", example = "123456", requiredMode = REQUIRED)
    String certificationCode
) {

}
