package in.koreatech.koin.admin.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubActiveChangeRequest(
    @Schema(description = "동아리 활성화 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "동아리 활성화 여부는 필수 입력사항입니다.")
    Boolean isActive
) {
    
}
