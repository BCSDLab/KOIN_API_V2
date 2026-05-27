package in.koreatech.koin.domain.shop.dto.event.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShopEventCountResponse(
    @Schema(example = "5", description = "이벤트 진행 중인 상점 개수", requiredMode = REQUIRED)
    Integer count
) {
}
