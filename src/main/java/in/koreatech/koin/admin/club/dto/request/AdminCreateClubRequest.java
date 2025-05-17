package in.koreatech.koin.admin.club.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubManager;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminCreateClubRequest(
    @Schema(description = "동아리 이름", example = "BCSD Lab", requiredMode = REQUIRED)
    @NotEmpty(message = "동아리 이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "동아리 사진 링크", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = REQUIRED)
    @NotNull(message = "동아리 사진은 필수 입력 사항입니다.")
    String imageUrl,

    @Schema(description = "동아리 관리자 ID 리스트", requiredMode = REQUIRED)
    @NotEmpty(message = "동아리 관리자는 필수 입력 사항입니다.")
    List<InnerClubManagerRequest> clubManagers,

    @Schema(description = "동아리 분과 카테고리 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "동아리 분과 카테고리는 필수 입력 사항입니다.")
    Integer clubCategoryId,

    @Schema(description = "동아리 위치", example = "학생회관", requiredMode = REQUIRED)
    @NotNull(message = "동아리 위치는 필수 입력 사항입니다.")
    String location,

    @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
    @NotNull(message = "동아리 소개는 필수 입력 사항입니다.")
    String description,

    @Schema(description = "인스타그램 링크", example = "https://www.instagram.com/bcsdlab/", requiredMode = REQUIRED)
    String instagram,

    @Schema(description = "구글 폼 링크", example = "https://forms.gle/example", requiredMode = REQUIRED)
    String googleForm,

    @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/example", requiredMode = REQUIRED)
    String openChat,

    @Schema(description = "전화번호", example = "01012345678", requiredMode = REQUIRED)
    String phoneNumber
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerClubManagerRequest(
        @Schema(description = "동아리 관리자 id", example = "bcsdlab", requiredMode = REQUIRED)
        String userId
    ) {
        public ClubManager toEntity(Club club, User user) {
            return ClubManager.builder()
                .club(club)
                .user(user)
                .build();
        }
    }

    public Club toEntity(ClubCategory clubCategory) {
        return Club.builder()
            .name(name)
            .lastWeekHits(0)
            .active(false)
            .likes(0)
            .hits(0)
            .introduction("")
            .imageUrl(imageUrl)
            .clubCategory(clubCategory)
            .description(description)
            .location(location)
            .build();
    }
}
