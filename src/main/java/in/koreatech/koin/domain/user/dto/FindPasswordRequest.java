package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record FindPasswordRequest(
    @Schema(description = "이메일 주소", example = "asdf@koreatech.ac.kr", requiredMode = REQUIRED)
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@koreatech.ac.kr$", message = "아우누리 계정 형식이 아닙니다. ${validatedValue}")
    @NotNull(message = "이메일을 입력해주세요.")
    String email
) {

}
