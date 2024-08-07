package in.koreatech.koin.domain.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandsResponse(
    @Schema(description = "상세 이름", example = "럭키빌(투베이)", requiredMode = NOT_REQUIRED)
    String internalName,

    @Schema(description = "월세", example = "220~250만원(6개월)", requiredMode = NOT_REQUIRED)
    String monthlyFee,

    @Schema(description = "위도", example = "36.769204", requiredMode = NOT_REQUIRED)
    Double latitude,

    @Schema(description = "보증금", example = "4500", requiredMode = NOT_REQUIRED)
    String charterFee,

    @Schema(description = "이름", example = "럭키빌(투베이)", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "복덕방 고유 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "경도", example = "127.282554", requiredMode = NOT_REQUIRED)
    Double longitude,

    @Schema(description = "방 종류", example = "투베이", requiredMode = NOT_REQUIRED)
    String roomType
) {

    public static LandsResponse from(Land land) {
        return new LandsResponse(
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
