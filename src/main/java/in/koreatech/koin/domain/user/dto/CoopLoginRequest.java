package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CoopLoginRequest(
    @Schema(description = "아이디", example = "koin123", requiredMode = REQUIRED)
    @NotBlank(message = "아이디를 입력해주세요.")
    String id,

    @Schema(
        description = "SHA 256 해시 알고리즘으로 암호화된 비밀번호",
        example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
        requiredMode = REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {

}
