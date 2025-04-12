package in.koreatech.koin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckPhoneDuplicationRequest(
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Schema(description = "전화번호", example = "01012345678")
    String phone
) {

}
