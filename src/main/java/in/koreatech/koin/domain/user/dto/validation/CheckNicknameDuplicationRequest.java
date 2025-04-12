package in.koreatech.koin.domain.user.dto.validation;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CheckNicknameDuplicationRequest(
    @Schema(description = "닉네임", example = "홍길동", requiredMode = REQUIRED)
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    @NotBlank(message = "닉네임을 입력해주세요.")
    String nickname
) {

}
