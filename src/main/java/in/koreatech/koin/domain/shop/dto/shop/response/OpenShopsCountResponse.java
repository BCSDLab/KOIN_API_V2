package in.koreatech.koin.domain.shop.dto.shop.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public record OpenShopsCountResponse(
    @Schema(example = "24", description = "현재 영업 중인 상점 개수", requiredMode = REQUIRED)
    Integer count
) {
}
