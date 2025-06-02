package in.koreatech.koin.domain.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubManagerEmpowermentRequest(
    @Schema(description = "동아리 아이디", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "동아리 아이디는 필수 입력사항입니다.")
    Integer clubId,

    @Schema(description = "위임받는 사용자의 아이디", example = "example", requiredMode = REQUIRED)
    @NotEmpty(message = "위임받는 사용자의 아이디를 입력해주세요.")
    String changedManagerId
) {
}
