package in.koreatech.koin.domain.coop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SoldOutRequest(
    @Schema(description = "메뉴 고유 ID", example = "1")
    Long menuId,

    @Schema(description = "품절 여부", example = "true")
    Boolean soldOut
) {

}
