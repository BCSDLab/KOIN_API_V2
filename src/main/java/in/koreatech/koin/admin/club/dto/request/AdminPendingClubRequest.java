package in.koreatech.koin.admin.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminPendingClubRequest(
    @Schema(description = "미승인 동아리 이름", example = "bcsd", requiredMode = REQUIRED)
    @NotBlank(message = "미승인 동아리 이름은 필수 입력사항입니다.")
    @Size(max = 20, message = "미승인 동아리 이름은 최대 20자 입니다.")
    String clubName
) {

}
