package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckUserIdRequest(
    @Schema(description = "사용자 ID", example = "user123", requiredMode = REQUIRED)
    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Pattern(regexp = "^[a-z0-9_.-]{1,13}$", message = "사용자 ID는 영소문자, 숫자, 밑줄(_), 하이픈(-), 마침표(.)로 이루어진 1~13자여야 합니다.")
    String userId
) {

}
