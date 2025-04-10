package in.koreatech.koin.domain.user.dto.verification;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CheckPhoneNumberRequest(
    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "올바른 전화번호 형식이 아닙니다.")
    String phoneNumber
) {

}
