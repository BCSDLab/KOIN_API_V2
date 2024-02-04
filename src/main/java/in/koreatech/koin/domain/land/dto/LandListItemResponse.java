package in.koreatech.koin.domain.land.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandListItemResponse(
    @Schema(description = "상세 이름", example = "럭키빌(투베이)")
    String internalName,

    @Schema(description = "월세", example = "220~250만원(6개월)")
    String monthlyFee,

    @Schema(description = "위도", example = "36.769204")
    Double latitude,

    @Schema(description = "보증금", example = "4500")
    String charterFee,

    @Schema(description = "이름", example = "럭키빌(투베이)")
    String name,

    @Schema(description = "복덕방 고유 ID", example = "1")
    Long id,

    @Schema(description = "경도", example = "127.282554")
    Double longitude,

    @Schema(description = "방 종류", example = "투베이")
    String roomType
) {

    public static LandListItemResponse from(Land land) {
        return new LandListItemResponse(
            land.getInternalName(),
            land.getMonthlyFee(),
            land.getLatitude(),
            land.getCharterFee(),
            land.getName(),
            land.getId(),
            land.getLongitude(),
            land.getRoomType()
        );
    }
}
