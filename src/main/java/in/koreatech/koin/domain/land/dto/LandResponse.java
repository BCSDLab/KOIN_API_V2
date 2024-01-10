package in.koreatech.koin.domain.land.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LandResponse(
    Boolean optElectronicDoorLocks,
    Boolean optTv,
    String monthlyFee,
    Boolean optElevator,
    Boolean optWaterPurifier,
    Boolean optWasher,
    Double latitude,
    String charterFee,
    Boolean optVeranda,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
    String description,
    List<String> imageUrls,
    Boolean optGasRange,
    Boolean optInduction,
    String internalName,
    Boolean isDeleted,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt,
    Boolean optBidet,
    Boolean optShoeCloset,
    Boolean optRefrigerator,
    Long id,
    Long floor,
    String managementFee,
    Boolean optDesk,
    Boolean optCloset,
    Double longitude,
    String address,
    Boolean optBed,
    Double size,
    String phone,
    Boolean optAirConditioner,
    String name,
    String deposit,
    Boolean optMicrowave,
    String permalink,
    String roomType) {

    public static LandResponse of(Land land, List<String> imageUrls, String permalink) {
        return new LandResponse(
            land.getOptElectronicDoorLocks(),
            land.getOptTv(),
            land.getMonthlyFee(),
            land.getOptElevator(),
            land.getOptWaterPurifier(),
            land.getOptWasher(),
            land.getLatitude(),
            land.getCharterFee(),
            land.getOptVeranda(),
            land.getCreatedAt(),
            land.getDescription(),
            imageUrls,
            land.getOptGasRange(),
            land.getOptInduction(),
            land.getInternalName(),
            land.getIsDeleted(),
            land.getUpdatedAt(),
            land.getOptBidet(),
            land.getOptShoeCloset(),
            land.getOptRefrigerator(),
            land.getId(),
            land.getFloor(),
            land.getManagementFee(),
            land.getOptDesk(),
            land.getOptCloset(),
            land.getLongitude(),
            land.getAddress(),
            land.getOptBed(),
            land.getSize(),
            land.getPhone(),
            land.getOptAirConditioner(),
            land.getName(),
            land.getDeposit(),
            land.getOptMicrowave(),
            permalink,
            land.getRoomType()
        );
    }
}
