package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EmailVerifyVerificationCodeRequest(
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "이메일", example = "test@koreatech.ac.kr", requiredMode = REQUIRED)
    String email,

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Digits(integer = 6, fraction = 0, message = "인증번호는 6자리 숫자여야 합니다.")
    @Schema(description = "인증번호", example = "123456", requiredMode = REQUIRED)
    String verificationCode
) {

}
