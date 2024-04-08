package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record FindPasswordRequest(
    @Email(message = "아우누리 계정 형식이 아닙니다. ${validatedValue}")
    @NotNull(message = "이메일은 비어있을 수 없습니다.")
    @Schema(description = "이메일 주소", requiredMode = REQUIRED, example = "asdf@koreatech.ac.kr")
    @JsonProperty(value = "address")
    String email
) {

}
