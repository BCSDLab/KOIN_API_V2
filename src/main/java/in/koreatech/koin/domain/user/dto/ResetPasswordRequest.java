package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin._common.validation.EmailOrPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @EmailOrPhone
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr")
    String verification,

    @Schema(description = "변경할 비밀번호 (SHA 256 해싱된 값)", example = "password", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {

}
