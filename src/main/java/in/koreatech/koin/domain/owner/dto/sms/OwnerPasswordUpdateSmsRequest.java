package in.koreatech.koin.domain.owner.dto.sms;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerPasswordUpdateSmsRequest(
    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", example = "01000000000", requiredMode = REQUIRED)
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @NotBlank(message = "비밀번호는 비워둘 수 없습니다.")
    @Schema(description = "비밀번호", example = "a0240120305812krlakdsflsa;1235", requiredMode = REQUIRED)
    String password
) {

}
