package in.koreatech.koin.domain.land.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandResponse(
    @Schema(description = "전자 도어 락 옵션", example = "true")
    Boolean optElectronicDoorLocks,

    @Schema(description = "TV 옵션", example = "true")
    Boolean optTv,

    @Schema(description = "월세 범위", example = "220~250만원(6개월)")
    String monthlyFee,

    @Schema(description = "엘리베이터 옵션", example = "true")
    Boolean optElevator,

    @Schema(description = "정수기 옵션", example = "true")
    Boolean optWaterPurifier,

    @Schema(description = "세탁기 옵션", example = "true")
    Boolean optWasher,

    @Schema(description = "위도 좌표", example = "36.769062")
    Double latitude,

    @Schema(description = "전세 가격", example = "4500")
    String charterFee,

    @Schema(description = "베란다 옵션", example = "true")
    Boolean optVeranda,

    @Schema(description = "생성 일시", example = "2020-08-19 13:44:11")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "설명", example = "null")
    String description,

    @Schema(description = "이미지 URL 목록", example = "null")
    List<String> imageUrls,

    @Schema(description = "가스 레인지 옵션", example = "false")
    Boolean optGasRange,

    @Schema(description = "인덕션 옵션", example = "true")
    Boolean optInduction,

    @Schema(description = "내부 이름", example = "행운빌(투베이)")
    String internalName,

    @Schema(description = "삭제 여부", example = "false")
    Boolean isDeleted,

    @Schema(description = "최근 업데이트 일시", example = "2021-05-19 23:07:13")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "비데 옵션", example = "false")
    Boolean optBidet,

    @Schema(description = "신발장 옵션", example = "true")
    Boolean optShoeCloset,

    @Schema(description = "냉장고 옵션", example = "true")
    Boolean optRefrigerator,

    @Schema(description = "고유 식별자", example = "68")
    Long id,

    @Schema(description = "층 수", example = "null")
    Long floor,

    @Schema(description = "인당 관리비", example = "21(1인 기준)")
    String managementFee,

    @Schema(description = "책상 옵션", example = "true")
    Boolean optDesk,

    @Schema(description = "옷장 옵션", example = "true")
    Boolean optCloset,

    @Schema(description = "경도 좌표", example = "127.28265")
    Double longitude,

    @Schema(description = "주소", example = "충남 천안시 동남구 병천면 가전6길 17-1")
    String address,

    @Schema(description = "침대 옵션", example = "true")
    Boolean optBed,

    @Schema(description = "크기(제곱 미터)", example = "9")
    Double size,

    @Schema(description = "전화번호", example = "010-3257-5598")
    String phone,

    @Schema(description = "에어컨 옵션", example = "true")
    Boolean optAirConditioner,

    @Schema(description = "부동산 이름", example = "행운빌 (투베이)")
    String name,

    @Schema(description = "보증금 금액", example = "50")
    String deposit,

    @Schema(description = "전자레인지 옵션", example = "true")
    Boolean optMicrowave,

    @Schema(description = "부동산 링크", example = "%ED%96%89%EC%9A%B4%EB%B9%8C%28%ED%88%AC%EB%B2%A0%EC%9D%B4%29")
    String permalink,

    @Schema(description = "방 유형", example = "투베이")
    String roomType
) {

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
