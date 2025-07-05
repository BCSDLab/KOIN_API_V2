package in.koreatech.koin.domain.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubSNS;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubResponse(
    @Schema(description = "동아리 카테고리 고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "동아리명", example = "BCSD", requiredMode = REQUIRED)
    String name,

    @Schema(description = "카테고리", example = "학술", requiredMode = REQUIRED)
    String category,

    @Schema(description = "동아리 위치", example = "학생식당 건물 406호", requiredMode = REQUIRED)
    String location,

    @Schema(description = "동아리 이미지 url", example = "https://static.koreatech.in/test.png", requiredMode = REQUIRED)
    String imageUrl,

    @Schema(description = "좋아요 수", example = "100", requiredMode = REQUIRED)
    Integer likes,

    @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
    String description,

    @Schema(description = "동아리 상세 소개", example = "안녕하세요 BCSDLab입니다", requiredMode = REQUIRED)
    String introduction,

    @Schema(description = "인스타그램 링크", example = "https://www.instagram.com/bcsdlab/")
    Optional<String> instagram,

    @Schema(description = "구글 폼 링크", example = "https://forms.gle/example")
    Optional<String> googleForm,

    @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/example")
    Optional<String> openChat,

    @Schema(description = "전화번호", example = "010-1234-5678")
    Optional<String> phoneNumber,

    @Schema(description = "동아리 관리자 여부", example = "true")
    Boolean manager,

    @Schema(description = "동아리 좋아요 여부", example = "true", requiredMode = REQUIRED)
    Boolean isLiked,

    @JsonFormat(pattern = "yyyy.MM.dd.")
    @Schema(description = "업데이트 날짜", example = "2025.05.11.", requiredMode = REQUIRED)
    LocalDate updatedAt,

    @Schema(description = "동아리 좋아요 숨김 여부", example = "false", requiredMode = REQUIRED)
    Boolean isLikeHidden,

    @Schema(description = "인기 동아리 메세지", example = "7월 1주차 인기 동아리 선정! 2주 연속 인기 동아리!", requiredMode = REQUIRED)
    String hotMessage
) {
    public static ClubResponse from(Club club, List<ClubSNS> clubSNSs, Boolean manager, Boolean isLiked) {
        Optional<String> instagram = Optional.empty();
        Optional<String> googleForm = Optional.empty();
        Optional<String> openChat = Optional.empty();
        Optional<String> phoneNumber = Optional.empty();

        for (ClubSNS sns : clubSNSs) {
            switch (sns.getSnsType()) {
                case INSTAGRAM -> instagram = Optional.of(sns.getContact());
                case GOOGLE_FORM -> googleForm = Optional.of(sns.getContact());
                case OPEN_CHAT -> openChat = Optional.of(sns.getContact());
                case PHONE_NUMBER -> phoneNumber = Optional.of(sns.getContact());
            }
        }

        return new ClubResponse(
            club.getId(),
            club.getName(),
            club.getClubCategory().getName(),
            club.getLocation(),
            club.getImageUrl(),
            club.getLikes(),
            club.getDescription(),
            club.getIntroduction(),
            instagram,
            googleForm,
            openChat,
            phoneNumber,
            manager,
            isLiked,
            club.getUpdatedAt().toLocalDate(),
            club.getIsLikeHidden(),
            null
        );
    }
    public static ClubResponse from(Club club, List<ClubSNS> clubSNSs, Boolean manager, Boolean isLiked, String hotMessage) {
        Optional<String> instagram = Optional.empty();
        Optional<String> googleForm = Optional.empty();
        Optional<String> openChat = Optional.empty();
        Optional<String> phoneNumber = Optional.empty();

        for (ClubSNS sns : clubSNSs) {
            switch (sns.getSnsType()) {
                case INSTAGRAM -> instagram = Optional.of(sns.getContact());
                case GOOGLE_FORM -> googleForm = Optional.of(sns.getContact());
                case OPEN_CHAT -> openChat = Optional.of(sns.getContact());
                case PHONE_NUMBER -> phoneNumber = Optional.of(sns.getContact());
            }
        }

        return new ClubResponse(
            club.getId(),
            club.getName(),
            club.getClubCategory().getName(),
            club.getLocation(),
            club.getImageUrl(),
            club.getLikes(),
            club.getDescription(),
            club.getIntroduction(),
            instagram,
            googleForm,
            openChat,
            phoneNumber,
            manager,
            isLiked,
            club.getUpdatedAt().toLocalDate(),
            club.getIsLikeHidden(),
            hotMessage
        );
    }
}
