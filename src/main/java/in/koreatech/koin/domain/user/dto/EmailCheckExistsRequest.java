package in.koreatech.koin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCheckExistsRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr")
    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email
) {
}
