package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = SnakeCaseStrategy.class)
public record VerifySmsVerificationRequest(
    @Schema(description = "전화번호", example = "01012345678", requiredMode = REQUIRED)
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Digits(integer = 6, fraction = 0, message = "인증번호는 6자리 숫자여야 합니다.")
    @Schema(description = "인증번호", example = "123456", requiredMode = REQUIRED)
    String verificationCode
) {

}
