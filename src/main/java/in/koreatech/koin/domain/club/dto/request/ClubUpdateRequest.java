package in.koreatech.koin.domain.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubUpdateRequest(
    @Schema(description = "동아리명", example = "BCSD", requiredMode = REQUIRED)
    @Size(max = 20, message = "동아리 이름은 최대 20자 입니다.")
    @NotEmpty(message = "동아리 이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "동아리 사진 링크", example = "https://bcsdlab.com/static/img/logo.d89d9cc.png", requiredMode = REQUIRED)
    @Size(max = 255, message = "동아리 사진 링크는 최대 255자 입니다.")
    @NotBlank(message = "동아리 사진은 필수 입력 사항입니다.")
    String imageUrl,

    @Schema(description = "동아리 분과 카테고리 ID", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "동아리 분과 카테고리는 필수 입력 사항입니다.")
    Integer clubCategoryId,

    @Schema(description = "동아리 위치", example = "학생회관", requiredMode = REQUIRED)
    @Size(max = 20, message = "동아리 위치는 최대 20자 입니다.")
    @NotBlank(message = "동아리 위치는 필수 입력 사항입니다.")
    String location,

    @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
    @Size(max = 40, message = "동아리 소개는 최대 40자 입니다.")
    String description,

    @Schema(description = "인스타그램 링크", example = "https://www.instagram.com/bcsdlab/", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "인스타그램 링크는 최대 255자 입니다.")
    String instagram,

    @Schema(description = "구글 폼 링크", example = "https://forms.gle/example", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "구글폼 링크는 최대 255자 입니다.")
    String googleForm,

    @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/example", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "오픈 채팅 링크는 최대 255자 입니다.")
    String openChat,

    @Schema(description = "전화번호", example = "01012345678", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "전화번호는 최대 255자 입니다.")
    @NotBlank(message = "전화번호는 필수 입력사항입니다.")
    String phoneNumber,

    @Schema(description = "동아리 좋아요 숨김 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "좋아요 숨김 여부는 필수 입력 사항입니다.")
    Boolean isLikeHidden
) {

    public Club toEntity(ClubCategory clubCategory) {
        return Club.builder()
            .name(name)
            .lastWeekHits(0)
            .isActive(false)
            .likes(0)
            .hits(0)
            .introduction("")
            .imageUrl(imageUrl)
            .clubCategory(clubCategory)
            .description(Objects.requireNonNullElse(description, ""))
            .location(location)
            .isLikeHidden(isLikeHidden)
            .build();
    }
}
