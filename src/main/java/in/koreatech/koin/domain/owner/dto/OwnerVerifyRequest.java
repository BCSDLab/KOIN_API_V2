package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerVerifyRequest(
    @JsonProperty(value = "address")
    @Email(message = "이메일 형식이 올바르지 않습니다. ${validatedValue}")
    @NotBlank(message = "이메일은 필수입니다.")
    @Schema(description = "사장님 이메일", example = "junho5336@gmail.com", requiredMode = REQUIRED)
    String email,

    @NotBlank(message = "인증 코드는 필수입니다.")
    @Digits(integer = 6, fraction = 0, message = "인증 코드는 6자리 정수여야 합니다. ${validatedValue}")
    @Schema(description = "인증 코드", example = "123456", requiredMode = REQUIRED)
    String certificationCode
) {

}
