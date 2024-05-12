package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record VerifyPhoneRequest(
    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = REQUIRED)
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 올바르지 않습니다.")
    String phoneNumber
) {

}
