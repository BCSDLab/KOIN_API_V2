package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record OwnerLoginRequest(
    @Schema(description = "전화번호", example = "01012345678", requiredMode = REQUIRED)
    @NotBlank(message = "전화번호를 입력해주세요.")
    String phoneNumber,

    @Schema
        (
            description = "SHA 256 해시 알고리즘으로 암호화된 비밀번호",
            example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
            requiredMode = REQUIRED
        )
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {

}
