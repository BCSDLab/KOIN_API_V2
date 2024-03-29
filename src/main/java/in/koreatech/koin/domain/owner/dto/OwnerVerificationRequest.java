package in.koreatech.koin.domain.owner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerVerificationRequest(
    @JsonProperty(value = "address")
    @Schema(description = "사장님 이메일", example = "junho5336@gmail.com")
    String email,

    @Schema(description = "인증 코드", example = "123456")
    String certificationCode
) {

}
