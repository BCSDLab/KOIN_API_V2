package in.koreatech.koin.domain.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.redis.ClubHotRedis;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubHotResponse(
    @Schema(description = "인기 동아리 ID", example = "1", requiredMode = REQUIRED)
    Integer clubId,

    @Schema(description = "동아리명", example = "BCSD", requiredMode = REQUIRED)
    String name,

    @Schema(description = "이미지url", example = "https://stage.koreatech.in/image.jpg", requiredMode = REQUIRED)
    String imageUrl
) {

    public static ClubHotResponse from(ClubHotRedis clubHotRedis) {
        return new ClubHotResponse(
            clubHotRedis.getClubId(),
            clubHotRedis.getName(),
            clubHotRedis.getImageUrl()
        );
    }

    public static ClubHotResponse from(Club club) {
        return new ClubHotResponse(
            club.getId(),
            club.getName(),
            club.getImageUrl()
        );
    }
}
