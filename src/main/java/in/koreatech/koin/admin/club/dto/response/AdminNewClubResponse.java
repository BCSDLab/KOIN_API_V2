package in.koreatech.koin.admin.club.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import java.util.Optional;

import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminNewClubResponse
    (
        @Schema(description = "동아리 이름", example = "BCSD", requiredMode = REQUIRED)
        String name,

        @Schema(description = "동아리 신청자 전화번호", example = "01012345678", requiredMode = REQUIRED)
        String requesterPhoneNumber,

        @Schema(description = "동아리 분과 카테고리", example = "학술", requiredMode = REQUIRED)
        String clubCategory,

        @Schema(description = "동아리 위치", example = "학생식당 건물 406호", requiredMode = REQUIRED)
        String location,

        @Schema(description = "동아리 이미지 url", example = "https://static.koreatech.in/test.png", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "동아리 소개", example = "즐겁게 일하고 열심히 노는 IT 특성화 동아리", requiredMode = REQUIRED)
        String description,

        @Schema(description = "인스타그램 링크", example = "https://www.instagram.com/bcsdlab/")
        Optional<String> instagram,

        @Schema(description = "구글 폼 링크", example = "https://forms.gle/example")
        Optional<String> googleForm,

        @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/example")
        Optional<String> openChat,

        @Schema(description = "동아리 관리자 전화번호", example = "01098765432")
        Optional<String> phoneNumber
    ) {
    public static AdminNewClubResponse from(ClubCreateRedis redis, User requester, String clubCategory) {
        return new AdminNewClubResponse(
            redis.getName(),
            requester.getPhoneNumber(),
            clubCategory,
            redis.getLocation(),
            redis.getImageUrl(),
            redis.getDescription(),
            Optional.ofNullable(redis.getInstagram()),
            Optional.ofNullable(redis.getGoogleForm()),
            Optional.ofNullable(redis.getOpenChat()),
            Optional.ofNullable(redis.getPhoneNumber())
        );
    }
}
