package in.koreatech.koin.domain.shop.dto.menu;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record MenuCategoriesResponse(
    @Schema(description = "카테고리 수", example = "3")
    Long count,

    @Schema(description = "카테고리 목록")
    List<MenuCategoryResponse> menuCategories
) {

    public static MenuCategoriesResponse from(List<MenuCategory> menuCategories) {
        List<MenuCategoryResponse> categories = menuCategories.stream()
            .map(menuCategory -> MenuCategoryResponse.of(menuCategory.getId(), menuCategory.getName()))
            .toList();

        return new MenuCategoriesResponse((long)categories.size(), categories);
    }

    private record MenuCategoryResponse(
        @Schema(description = "카테고리 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "카테고리 이름", example = "치킨", requiredMode = REQUIRED)
        String name
    ) {

        public static MenuCategoryResponse of(Integer id, String name) {
            return new MenuCategoryResponse(id, name);
        }
    }
}
