package in.koreatech.koin.domain.user.dto.verification;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CheckUserIdWithPhoneNumberRequest(
    @Schema(description = "사용자 ID", example = "user123")
    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "사용자 ID는 4~20자의 영문자와 숫자만 가능합니다.")
    String userId,

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber
) {

}
