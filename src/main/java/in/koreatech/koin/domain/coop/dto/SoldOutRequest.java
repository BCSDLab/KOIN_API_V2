package in.koreatech.koin.domain.coop.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SoldOutRequest(
    @Schema(description = "메뉴 고유 ID", example = "1")
    Integer menuId,

    @NotNull
    @Schema(description = "품절 여부", example = "true")
    Boolean soldOut
) {

    @Builder
    public SoldOutRequest(Integer menuId, Boolean soldOut) {
        this.menuId = menuId;
        this.soldOut = soldOut;
    }
}
