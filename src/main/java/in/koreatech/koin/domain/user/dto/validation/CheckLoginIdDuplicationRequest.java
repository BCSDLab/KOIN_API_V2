package in.koreatech.koin.domain.user.dto.validation;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CheckLoginIdDuplicationRequest(
    @Schema(description = "로그인 Id", example = "example012", requiredMode = REQUIRED)
    @NotBlank(message = "아이디를 입력해주세요.")
    String loginId
) {

}
