package in.koreatech.koin.domain.land.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.domain.land.model.Land;

public record LandResponse(
    @JsonProperty("internal_name") String internalName,
    @JsonProperty("monthly_fee") String monthlyFee,
    @JsonProperty("latitude") String latitude,
    @JsonProperty("charter_fee") String charterFee,
    @JsonProperty("name") String name,
    @JsonProperty("id") Long id,
    @JsonProperty("longitude") String longitude,
    @JsonProperty("room_type") String roomType) {

    public static LandResponse from(Land land) {
        return new LandResponse(
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
