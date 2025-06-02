package in.koreatech.koin.domain.club.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubsByCategoryResponse(
    @Schema(description = "동아리 리스트", requiredMode = REQUIRED)
    List<InnerClubResponse> clubs
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerClubResponse(
        @Schema(description = "동아리 고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
        String name,

        @Schema(description = "동아리 카테고리", example = "학술", requiredMode = REQUIRED)
        String category,

        @Schema(description = "동아리 좋아요 수", example = "9999999", requiredMode = REQUIRED)
        Integer likes,

        @Schema(description = "동아리 이미지 url", example = "https://static.koreatech.in/test.png", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "동아리 좋아요 여부", example = "true", requiredMode = REQUIRED)
        Boolean isLiked,

        @Schema(description = "동아리 좋아요 숨김 여부", example = "false", requiredMode = REQUIRED)
        Boolean isLikeHidden
    ) {
        private static InnerClubResponse from(Club club, Boolean isLiked) {
            return new InnerClubResponse(
                club.getId(),
                club.getName(),
                club.getClubCategory().getName(),
                club.getLikes(),
                club.getImageUrl(),
                isLiked,
                club.getIsLikeHidden()
            );
        }
    }

    public static ClubsByCategoryResponse from(List<Club> clubs, List<Integer> likedClubIds) {
        return new ClubsByCategoryResponse(
            clubs.stream()
                .map(club -> InnerClubResponse.from(club, likedClubIds.contains(club.getId())))
                .toList()
        );
    }
}
