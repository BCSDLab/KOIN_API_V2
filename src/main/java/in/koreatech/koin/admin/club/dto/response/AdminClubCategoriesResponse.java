package in.koreatech.koin.admin.club.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.club.category.model.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubCategoriesResponse(
    @Schema(description = "동아리 카테고리 리스트", requiredMode = REQUIRED)
    List<InnerAdminClubCategoryResponse> clubCategories
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminClubCategoryResponse(
        @Schema(description = "동아리 카테고리 고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "동아리 카테고리 이름", example = "학술", requiredMode = REQUIRED)
        String name
    ) {
        private static InnerAdminClubCategoryResponse from(ClubCategory category) {
            return new InnerAdminClubCategoryResponse(category.getId(), category.getName());
        }
    }

    public static AdminClubCategoriesResponse from(List<ClubCategory> clubCategories) {
        return new AdminClubCategoriesResponse(clubCategories.stream()
            .map(InnerAdminClubCategoryResponse::from)
            .toList()
        );
    }
}
