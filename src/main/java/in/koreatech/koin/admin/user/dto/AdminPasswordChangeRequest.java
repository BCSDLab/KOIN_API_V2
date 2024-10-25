package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminPasswordChangeRequest(
    @Schema(description = "현재 비밀번호 (SHA 256 해싱된 값)", example = "password", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String oldPassword,

    @Schema(description = "변경할 비밀번호 (SHA 256 해싱된 값)", example = "password", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String newPassword
) {
}
