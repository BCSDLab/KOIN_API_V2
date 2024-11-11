package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.NotBlankElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminModifyShopCategoriesOrderRequest(
    @Schema(example = "[1, 2, 3]", description = "상점 카테고리 id 리스트 순서", requiredMode = REQUIRED)
    @NotNull(message = "상점 카테고리 id 리스트는 필수입니다.")
    @NotBlankElement(message = "빈 요소가 존재할 수 없습니다.")
    List<Integer> shopCategoryIds
) {

}
