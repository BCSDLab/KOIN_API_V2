package in.koreatech.koin.admin.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminLandResponse(
    @Schema(description = "고유 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer id,

    @Schema(description = "이름", example = "금실타운", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "내부 이름", example = "금실타운")
    String internalName,

    @Schema(description = "크기", example = "9.0")
    Double size,

    @Schema(description = "종류", example = "원룸")
    String roomType,

    @Schema(description = "위도", example = "36.766205")
    Double latitude,

    @Schema(description = "경도", example = "127.284638")
    Double longitude,

    @Schema(description = "전화번호", example = "041-111-1111")
    String phone,

    @Schema(description = "이미지 URL 리스트")
    List<String> imageUrls,

    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면")
    String address,

    @Schema(description = "설명", example = "1년 계약시 20만원 할인")
    String description,

    @Schema(description = "층수", example = "4")
    Integer floor,

    @Schema(description = "보증금", example = "30")
    String deposit,

    @Schema(description = "월세", example = "200만원 (6개월)")
    String monthlyFee,

    @Schema(description = "전세", example = "3500")
    String charterFee,

    @Schema(description = "관리비", example = "21(1인 기준)")
    String managementFee,

    @Schema(description = "냉장고 보유 여부", example = "true")
    boolean optRefrigerator,

    @Schema(description = "옷장 보유 여부", example = "true")
    boolean optCloset,

    @Schema(description = "TV 보유 여부", example = "true")
    boolean optTv,

    @Schema(description = "전자레인지 보유 여부", example = "true")
    boolean optMicrowave,

    @Schema(description = "가스레인지 보유 여부", example = "false")
    boolean optGasRange,

    @Schema(description = "인덕션 보유 여부", example = "true")
    boolean optInduction,

    @Schema(description = "정수기 보유 여부", example = "true")
    boolean optWaterPurifier,

    @Schema(description = "에어컨 보유 여부", example = "true")
    boolean optAirConditioner,

    @Schema(description = "세탁기 보유 여부", example = "true")
    boolean optWasher,

    @Schema(description = "침대 보유 여부", example = "false")
    boolean optBed,

    @Schema(description = "책상 보유 여부", example = "true")
    boolean optDesk,

    @Schema(description = "신발장 보유 여부", example = "true")
    boolean optShoeCloset,

    @Schema(description = "전자 도어락 보유 여부", example = "true")
    boolean optElectronicDoorLocks,

    @Schema(description = "비데 보유 여부", example = "false")
    boolean optBidet,

    @Schema(description = "베란다 보유 여부", example = "false")
    boolean optVeranda,

    @Schema(description = "엘리베이터 보유 여부", example = "true")
    boolean optElevator,

    @Schema(description = "삭제(soft delete) 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean isDeleted
) {
    public static AdminLandResponse from(Land land) {
        return new AdminLandResponse(
            land.getId(),
            land.getName(),
            land.getInternalName(),
            land.getSize(),
            land.getRoomType(),
            land.getLatitude(),
            land.getLongitude(),
            land.getPhone(),
            convertToList(land.getImageUrls()),
            land.getAddress(),
            land.getDescription(),
            land.getFloor(),
            land.getDeposit(),
            land.getMonthlyFee(),
            land.getCharterFee(),
            land.getManagementFee(),
            land.isOptRefrigerator(),
            land.isOptCloset(),
            land.isOptTv(),
            land.isOptMicrowave(),
            land.isOptGasRange(),
            land.isOptInduction(),
            land.isOptWaterPurifier(),
            land.isOptAirConditioner(),
            land.isOptWasher(),
            land.isOptBed(),
            land.isOptDesk(),
            land.isOptShoeCloset(),
            land.isOptElectronicDoorLocks(),
            land.isOptBidet(),
            land.isOptVeranda(),
            land.isOptElevator(),
            land.isDeleted()
        );
    }

    private static List<String> convertToList(String imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        return List.of(imageUrls.replace("[", "").replace("]", "").replace("\"", "").split(","));
    }
}
