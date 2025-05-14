package in.koreatech.koin.domain.club.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import in.koreatech.koin.domain.club.model.Club;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetClubByCategoryResponse(
    @Schema(description = "동아리 리스트", requiredMode = REQUIRED)
    List<GetClubByCategoryResponse.InnerClubResponse> clubCategories
) {
    public record InnerClubResponse(
        @Schema(description = "동아리 카테고리 고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "동아리 이름", example = "학술", requiredMode = REQUIRED)
        String name,

        @Schema(description = "카테고리", example = "학술", requiredMode = REQUIRED)
        String category,

        @Schema(description = "동아리 이미지 url", example = "https://static.koreatech.in/test.png", requiredMode = REQUIRED)
        String imageUrl
    ) {
        private static GetClubByCategoryResponse.InnerClubResponse from(Club club) {
            return new GetClubByCategoryResponse.InnerClubResponse(
                club.getId(), club.getName(), club.getClubCategory().getName(), club.getImageUrl()
            );
        }
    }

    public static GetClubByCategoryResponse from(List<Club> clubs) {
        return new GetClubByCategoryResponse(clubs.stream()
            .map(GetClubByCategoryResponse.InnerClubResponse::from)
            .toList()
        );
    }
}
