package in.koreatech.koin.domain.shop.dto.menu.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.koreatech.koin.domain.translation.service.TranslationSourceFields;
import in.koreatech.koin.domain.translation.service.TranslationSourceVersion;

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
        @JsonProperty("translation_source_version")
        @Schema(description = "이 메뉴 분류 이름의 번역 원문 버전")
        public String getTranslationSourceVersion() {
            return TranslationSourceVersion.of(TranslationSourceFields.name(name));
        }


        public static MenuCategoryResponse of(Integer id, String name) {
            return new MenuCategoryResponse(id, name);
        }
    }
}
