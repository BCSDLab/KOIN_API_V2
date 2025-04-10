package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SmsFindIdRequest(
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    @Schema(description = "전화번호", example = "01012345678", requiredMode = REQUIRED)
    String phoneNumber
) {

}
