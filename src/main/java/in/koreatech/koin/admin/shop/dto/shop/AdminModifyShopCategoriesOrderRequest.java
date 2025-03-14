package in.koreatech.koin.admin.shop.dto.shop;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminModifyShopCategoriesOrderRequest(
    @Schema(example = "[1, 2, 3]", description = "상점 카테고리 id 리스트 순서", requiredMode = REQUIRED)
    @NotNull(message = "상점 카테고리 id 리스트는 필수입니다.")
    List<Integer> shopCategoryIds
) {

}
