package in.koreatech.koin.domain.dining.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DiningLikeRequest(
    @Schema(description = "메뉴 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer diningId,

    @Schema(description = "사용자 ID", example = "1", requiredMode = REQUIRED)
    Integer userId
) {

}
