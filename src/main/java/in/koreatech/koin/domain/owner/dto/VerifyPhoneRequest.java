package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record VerifyPhoneRequest(
    @Size(min = 10, max = 11)
    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Schema(description = "휴대폰번호", example = "01084084888", requiredMode = REQUIRED)
    String phoneNumber
) {

}
