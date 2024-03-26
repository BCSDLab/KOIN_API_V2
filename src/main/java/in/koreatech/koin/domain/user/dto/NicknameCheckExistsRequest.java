package in.koreatech.koin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameCheckExistsRequest(
    @Schema(description = "닉네임", example = "홍길동")
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String nickname
) {

}
