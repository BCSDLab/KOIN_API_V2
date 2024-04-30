package in.koreatech.koin.domain.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LandResponse(
    @Schema(description = "전자 도어 락 옵션", example = "true", requiredMode = REQUIRED)
    boolean optElectronicDoorLocks,

    @Schema(description = "TV 옵션", example = "true", requiredMode = REQUIRED)
    boolean optTv,

    @Schema(description = "월세 범위", example = "220~250만원(6개월)", requiredMode = NOT_REQUIRED)
    String monthlyFee,

    @Schema(description = "엘리베이터 옵션", example = "true", requiredMode = REQUIRED)
    boolean optElevator,

    @Schema(description = "정수기 옵션", example = "true", requiredMode = REQUIRED)
    boolean optWaterPurifier,

    @Schema(description = "세탁기 옵션", example = "true", requiredMode = REQUIRED)
    boolean optWasher,

    @Schema(description = "위도 좌표", example = "36.769062", requiredMode = NOT_REQUIRED)
    Double latitude,

    @Schema(description = "전세 가격", example = "4500", requiredMode = NOT_REQUIRED)
    String charterFee,

    @Schema(description = "베란다 옵션", example = "true", requiredMode = REQUIRED)
    boolean optVeranda,

    @Schema(description = "생성 일시", example = "2020-08-19 13:44:11", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "설명", example = "한기대 정문 근처 원룸", requiredMode = NOT_REQUIRED)
    String description,

    @Schema(description = "이미지 URL 목록", example = """
        ["https://image.com", "https://image2.com"]
        """, requiredMode = REQUIRED)
    List<String> imageUrls,

    @Schema(description = "가스 레인지 옵션", example = "false", requiredMode = REQUIRED)
    boolean optGasRange,

    @Schema(description = "인덕션 옵션", example = "true", requiredMode = REQUIRED)
    boolean optInduction,

    @Schema(description = "내부 이름", example = "행운빌(투베이)", requiredMode = NOT_REQUIRED)
    String internalName,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    boolean isDeleted,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "최근 업데이트 일시", example = "2021-05-19 23:07:13", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "비데 옵션", example = "false", requiredMode = REQUIRED)
    boolean optBidet,

    @Schema(description = "신발장 옵션", example = "true", requiredMode = REQUIRED)
    boolean optShoeCloset,

    @Schema(description = "냉장고 옵션", example = "true", requiredMode = REQUIRED)
    boolean optRefrigerator,

    @Schema(description = "고유 식별자", example = "68", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "층 수", example = "null", requiredMode = NOT_REQUIRED)
    Integer floor,

    @Schema(description = "인당 관리비", example = "21(1인 기준)", requiredMode = NOT_REQUIRED)
    String managementFee,

    @Schema(description = "책상 옵션", example = "true", requiredMode = REQUIRED)
    boolean optDesk,

    @Schema(description = "옷장 옵션", example = "true", requiredMode = REQUIRED)
    boolean optCloset,

    @Schema(description = "경도 좌표", example = "127.28265", requiredMode = NOT_REQUIRED)
    Double longitude,

    @Schema(description = "주소", example = "충남 천안시 동남구 병천면 가전6길 17-1", requiredMode = NOT_REQUIRED)
    String address,

    @Schema(description = "침대 옵션", example = "true", requiredMode = REQUIRED)
    boolean optBed,

    @Schema(description = "크기(제곱 미터)", example = "9", requiredMode = NOT_REQUIRED)
    String size,

    @Schema(description = "전화번호", example = "010-3257-5598", requiredMode = NOT_REQUIRED)
    String phone,

    @Schema(description = "에어컨 옵션", example = "true", requiredMode = REQUIRED)
    boolean optAirConditioner,

    @Schema(description = "부동산 이름", example = "행운빌 (투베이)", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "보증금 금액", example = "50", requiredMode = NOT_REQUIRED)
    String deposit,

    @Schema(description = "전자레인지 옵션", example = "true", requiredMode = REQUIRED)
    boolean optMicrowave,

    @Schema(description = "부동산 링크", example = "%ED%96%89%EC%9A%B4%EB%B9%8C%28%ED%88%AC%EB%B2%A0%EC%9D%B4%29", requiredMode = NOT_REQUIRED)
    String permalink,

    @Schema(description = "방 유형", example = "투베이", requiredMode = NOT_REQUIRED)
    String roomType
) {

    public static LandResponse of(Land land, List<String> imageUrls, String permalink) {
        return new LandResponse(
            land.isOptElectronicDoorLocks(),
            land.isOptTv(),
            land.getMonthlyFee(),
            land.isOptElevator(),
            land.isOptWaterPurifier(),
            land.isOptWasher(),
            land.getLatitude(),
            land.getCharterFee(),
            land.isOptVeranda(),
            land.getCreatedAt(),
            land.getDescription(),
            imageUrls,
            land.isOptGasRange(),
            land.isOptInduction(),
            land.getInternalName(),
            land.isDeleted(),
            land.getUpdatedAt(),
            land.isOptBidet(),
            land.isOptShoeCloset(),
            land.isOptRefrigerator(),
            land.getId(),
            land.getFloor(),
            land.getManagementFee(),
            land.isOptDesk(),
            land.isOptCloset(),
            land.getLongitude(),
            land.getAddress(),
            land.isOptBed(),
            land.getSize(),
            land.getPhone(),
            land.isOptAirConditioner(),
            land.getName(),
            land.getDeposit(),
            land.isOptMicrowave(),
            permalink,
            land.getRoomType()
        );
    }
}
