package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminCreateShopCategoryRequest(
    @Schema(description = "이미지 URL", example = "https://static.koreatech.in/test.png", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "이미지 URL은 필수입니다.")
    @Size(max = 255, message = "이미지 URL은 255자 이하로 입력해주세요.")
    String imageUrl,

    @Schema(description = "이름", example = "햄버거", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(min = 1, max = 25, message = "이름은 1자 이상, 25자 이하로 입력해주세요.")
    String name,

    @Schema(description = "상위 카테고리 id", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "상위 카테고리는 필수입니다.")
    Integer parentCategoryId,

    @Schema(description = "이벤트 이미지 URL", example = "https://static.koreatech.in/test.png")
    @Size(max = 255, message = "이벤트 이미지 URL은 255자 이하로 입력해주세요.")
    String eventImageUrl
) {

    public ShopCategory toShopCategory(ShopParentCategory shopParentCategory) {
        return ShopCategory.builder()
            .imageUrl(imageUrl)
            .name(name)
            .parentCategory(shopParentCategory)
            .eventImageUrl(eventImageUrl)
            .build();
    }
}

