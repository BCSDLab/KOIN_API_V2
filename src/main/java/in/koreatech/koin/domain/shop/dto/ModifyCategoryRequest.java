package in.koreatech.koin.domain.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ModifyCategoryRequest(
    @Schema(example = "1", description = "상점 카테고리 고유 id")
    @NotNull Long id,

    @Schema(example = "사이드 메뉴", description = "카테고리 명")
    String name
) {
}
