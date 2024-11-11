package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminPasswordCheckRequest(
    @Schema(description = "256 SHA 알고리즘으로 암호화된 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password
) {

}
