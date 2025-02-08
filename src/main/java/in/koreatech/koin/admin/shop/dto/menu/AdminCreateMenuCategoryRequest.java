package in.koreatech.koin.admin.shop.dto.menu;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateMenuCategoryRequest(
    @Schema(example = "사이드 메뉴", description = "카테고리명", requiredMode = REQUIRED)
    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(min = 1, max = 20, message = "카테고리명은 1자 이상 20자 이하로 입력해주세요.")
    String name
) {

}
