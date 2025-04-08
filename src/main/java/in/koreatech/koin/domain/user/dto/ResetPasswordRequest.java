package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.validation.EmailOrPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record ResetPasswordRequest(
    @Size(max = 50, message = "아이디는 50자 이내여야 합니다.")
    @Schema(description = "사용자 아이디", example = "example123", requiredMode = NOT_REQUIRED)
    String userId,

    @EmailOrPhone
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr", requiredMode = REQUIRED)
    String target,

    @Schema(description = "변경할 비밀번호 (SHA 256 해싱된 값)", example = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String newPassword
) {

}
