package in.koreatech.koin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr")
    @Email(message = "이메일 형식을 지켜주세요.") @NotBlank(message = "이메일을 입력해주세요.") String email,

    @Schema(description = """
        SHA 256 해시 알고리즘으로 암호화된 비밀번호
        example: asdf1234!
        """, example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268")
    @NotBlank(message = "비밀번호를 입력해주세요.") String password
) {

}
