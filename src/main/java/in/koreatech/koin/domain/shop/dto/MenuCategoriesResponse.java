package in.koreatech.koin.domain.shop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.MenuCategory;

@JsonNaming(value = SnakeCaseStrategy.class)
public record MenuCategoriesResponse(
    Long count,
    List<MenuCategoryResponse> menuCategories
) {

    public static MenuCategoriesResponse from(List<MenuCategory> menuCategories) {
        List<MenuCategoryResponse> categories = menuCategories.stream()
            .map(menuCategory -> MenuCategoryResponse.of(menuCategory.getId(), menuCategory.getName()))
            .toList();

        return new MenuCategoriesResponse((long)categories.size(), categories);
    }

    private record MenuCategoryResponse(
        Integer id,
        String name
    ) {

        public static MenuCategoryResponse of(Integer id, String name) {
            return new MenuCategoryResponse(id, name);
        }
    }
}
