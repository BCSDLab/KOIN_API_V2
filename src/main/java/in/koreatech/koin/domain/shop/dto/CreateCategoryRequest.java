package in.koreatech.koin.domain.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
    @Schema(example = "사이드 메뉴", description = "카테고리명")
    @NotBlank @Size(min = 1, max = 15) String name
) {
}
