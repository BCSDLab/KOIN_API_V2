package in.koreatech.koin.domain.owner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerPasswordResetVerifyRequest(
    @JsonProperty(value = "address")
    @Schema(description = "사장님 이메일", example = "junho5336@gmail.com")
    String email,

    @NotBlank(message = "인증 코드는 필수입니다.")
    @Digits(integer = 6, fraction = 0, message = "인증 코드는 6자리 정수여야 합니다.")
    @Schema(description = "인증 코드", example = "123456")
    String certificationCode
) {

}
