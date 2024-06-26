package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AdminModifyMenuCategoryRequest(
    @Schema(example = "1", description = "상점 카테고리 고유 id", requiredMode = REQUIRED)
    @NotNull(message = "카테고리 ID는 필수입니다.")
    Integer id,

    @Schema(example = "사이드 메뉴", description = "카테고리 명", requiredMode = REQUIRED)
    @NotNull(message = "카테고리 명은 필수입니다.")
    String name
) {

}
