package in.koreatech.koin.domain.coop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SoldOutRequest(
    @Schema(description = "메뉴 고유 ID", example = "1") Long menuId,

    @Schema(description = "품절 여부", example = "true") Boolean soldOut
) {

}
