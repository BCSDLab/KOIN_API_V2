package in.koreatech.koin.domain.club.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.ClubBaseInfo;
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
        Boolean isLikeHidden,

        @Schema(description = "동아리 모집 정보", requiredMode = REQUIRED)
        InnerClubRecruitmentResponse recruitmentInfo
    ) {
        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerClubRecruitmentResponse(
            @Schema(description = "동아리 모집 상태", example = "ALWAYS", requiredMode = REQUIRED)
            String status,

            @Schema(description = "동아리 모집 디데이", example = "null", requiredMode = NOT_REQUIRED)
            Integer Dday
        ) {
            public static InnerClubRecruitmentResponse from(ClubBaseInfo clubBaseInfo) {
                return new InnerClubRecruitmentResponse(
                    clubBaseInfo.getRecruitmentStatus().name(),
                    clubBaseInfo.getRecruitmentPeriod()
                );
            }
        }

        private static InnerClubResponse from(ClubBaseInfo clubBaseInfo) {
            return new InnerClubResponse(
                clubBaseInfo.clubId(),
                clubBaseInfo.name(),
                clubBaseInfo.category(),
                clubBaseInfo.likes(),
                clubBaseInfo.imageUrl(),
                clubBaseInfo.isLiked(),
                clubBaseInfo.isLikeHidden(),
                InnerClubRecruitmentResponse.from(clubBaseInfo)
            );
        }
    }

    public static ClubsByCategoryResponse from(List<ClubBaseInfo> clubBaseInfos) {
        return new ClubsByCategoryResponse(
            clubBaseInfos.stream()
                .map(InnerClubResponse::from)
                .toList()
        );
    }
}
