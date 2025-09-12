package in.koreatech.koin.domain.club.category.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.category.model.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubCategoriesResponse(
    @Schema(description = "동아리 카테고리 리스트", requiredMode = REQUIRED)
    List<InnerClubCategoryResponse> clubCategories
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerClubCategoryResponse(
        @Schema(description = "동아리 카테고리 고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "동아리 카테고리 이름", example = "학술", requiredMode = REQUIRED)
        String name
    ) {
        private static InnerClubCategoryResponse from(ClubCategory category) {
            return new InnerClubCategoryResponse(category.getId(), category.getName());
        }
    }

    public static ClubCategoriesResponse from(List<ClubCategory> clubCategories) {
        return new ClubCategoriesResponse(clubCategories.stream()
            .map(InnerClubCategoryResponse::from)
            .toList()
        );
    }
}
