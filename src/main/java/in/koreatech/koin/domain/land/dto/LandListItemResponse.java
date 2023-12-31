package in.koreatech.koin.domain.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandListItemResponse(
    String internalName,
    String monthlyFee,
    String latitude,
    String charterFee,
    String name,
    Long id,
    String longitude,
    String roomType) {

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
