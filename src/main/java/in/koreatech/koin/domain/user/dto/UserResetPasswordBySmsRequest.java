package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserResetPasswordBySmsRequest(
    @Schema(description = "로그인 ID", example = "user123", requiredMode = REQUIRED)
    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Pattern(regexp = "^[a-z0-9_.-]{1,13}$", message = "로그인 ID는 영소문자, 숫자, 밑줄(_), 하이픈(-), 마침표(.)로 이루어진 1~13자여야 합니다.")
    String loginId,

    @Schema(description = "전화번호", example = "01012345678", requiredMode = REQUIRED)
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber,

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Schema(description = "새 비밀번호", example = "newPassword123!", requiredMode = REQUIRED)
    String newPassword
) {

}
