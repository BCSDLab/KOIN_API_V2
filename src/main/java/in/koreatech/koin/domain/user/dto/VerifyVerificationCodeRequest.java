package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin._common.validation.EmailOrPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

public record VerifyVerificationCodeRequest(
    @EmailOrPhone
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr")
    String target,

    @NotBlank(message = "인증 코드는 필수입니다.")
    @Digits(integer = 6, fraction = 0, message = "인증 코드는 6자리 정수여야 합니다. ${validatedValue}")
    @Schema(description = "인증 코드", example = "123456", requiredMode = REQUIRED)
    String code
) {

}
