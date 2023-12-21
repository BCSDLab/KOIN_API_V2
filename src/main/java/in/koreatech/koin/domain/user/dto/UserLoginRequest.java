package in.koreatech.koin.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "이메일을 입력해주세요.") String email,
    @NotBlank(message = "비밀번호를 입력해주세요.") String password
) {
}
