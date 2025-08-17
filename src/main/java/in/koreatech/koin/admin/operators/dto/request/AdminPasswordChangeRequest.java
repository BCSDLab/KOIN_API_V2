package in.koreatech.koin.admin.operators.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminPasswordChangeRequest(
    @Schema(description = "256 SHA 알고리즘으로 암호화된 현재 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268", requiredMode = REQUIRED)
    @NotBlank(message = "현재 비밀번호는 필수 입력사항 입니다.")
    String oldPassword,

    @Schema(description = "256 SHA 알고리즘으로 암호화된 새로운 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268", requiredMode = REQUIRED)
    @NotBlank(message = "새로운 비밀번호는 필수 입력사항 입니다.")
    String newPassword
) {
}
