package in.koreatech.koin.domain.owner.dto.sms;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerAccountCheckExistsRequest(
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    @NotBlank(message = "아이디를 입력해주세요.")
    @Schema(description = "아이디(전화번호)", example = "01012345678")
    String account
) {

}
