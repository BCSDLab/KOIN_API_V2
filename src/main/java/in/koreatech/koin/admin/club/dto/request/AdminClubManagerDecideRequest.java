package in.koreatech.koin.admin.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.validation.NotEmoji;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubManagerDecideRequest(
    @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
    @Size(max = 20, message = "동아리 이름은 최대 20자 입니다.")
    @NotBlank(message = "동아리 이름은 필수 입력 사항입니다.")
    @NotEmoji(message = "동아리 이름에는 이모지가 들어갈 수 없습니다.")
    String clubName,

    @Schema(description = "동아리 관리자 승인 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "동아리 관리자 승인 여부는 필수 입력사항입니다.")
    Boolean isAccept
) {

}
