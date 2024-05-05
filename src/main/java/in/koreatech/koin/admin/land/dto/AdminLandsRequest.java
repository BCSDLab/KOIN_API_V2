package in.koreatech.koin.admin.land.dto;


import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminLandsRequest(
    @Schema(description = "이름 - not null - 최대 255자", example = "금실타운", requiredMode = REQUIRED)
    @NotNull(message = "방이름은 필수입니다.")
    @Size(max = 255, message = "방이름의 최대 길이는 255자입니다.")
    String name,

    @Schema(description = "이름 - not null - 최대 50자", example = "금실타운", requiredMode = REQUIRED)
    @NotNull(message = "방이름은 필수입니다.")
    @Size(max = 50, message = "방이름의 최대 길이는 50자입니다.")
    String internalName,

    @Schema(description = "크기", example = "9.0")
    String size,

    @Schema(description = "종류 - 최대 20자", example = "원룸")
    @Size(max = 20, message = "방종류의 최대 길이는 20자입니다.")
    String roomType,

    @Schema(description = "위도", example = "36.766205")
    String latitude,

    @Schema(description = "경도", example = "127.284638")
    String longitude,

    @Schema(description = "전화번호 - 정규식 `^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$` 을 만족해야함", example = "041-111-1111")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$", message = "전화번호의 형식이 올바르지 않습니다.")
    String phone,

    @Schema(description = "이미지 url 리스트")
    List<String> imageUrls,

    @Schema(description = "주소 - 최대 65535자", example = "충청남도 천안시 동남구 병천면")
    @Size(max = 65535, message = "주소의 최대 길이는 65535자입니다.")
    String address,

    @Schema(description = "설명 - 최대 65535자", example = "1년 계약시 20만원 할인")
    @Size(max = 65535, message = "설명의 최대 길이는 65535자입니다.")
    String description,

    @Schema(description = "층수 - 음수 불가능", example = "4")
    @PositiveOrZero(message = "층수는 0 이상이어야합니다.")
    Integer floor,

    @Schema(description = "보증금 - 최대 255자", example = "30")
    @Size(max = 255, message = "보증금의 최대 길이는 255자입니다.")
    String deposit,

    @Schema(description = "월세 - 최대 255자", example = "200만원 (6개월)")
    @Size(max = 255, message = "월세의 최대 길이는 255자입니다.")
    String monthlyFee,

    @Schema(description = "전세 - 최대 20자", example = "3500")
    @Size(max = 20, message = "전세의 최대 길이는 20자입니다.")
    String charterFee,

    @Schema(description = "관리비 - 최대 255자", example = "21(1인 기준)")
    @Size(max = 255, message = "관리비의 최대 길이는 255자입니다.")
    String managementFee,

    @Schema(description = "냉장고 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optRefrigerator,

    @Schema(description = "옷장 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optCloset,

    @Schema(description = "tv 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optTv,

    @Schema(description = "전자레인지 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optMicrowave,

    @Schema(description = "가스레인지 보유 여부 - null일경우 false로 요청됨", example = "false")
    Boolean optGasRange,

    @Schema(description = "인덕션 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optInduction,

    @Schema(description = "정수기 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optWaterPurifier,

    @Schema(description = "에어컨 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optAirConditioner,

    @Schema(description = "샤워기 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optWasher
) {
    public Land toLand() {
        return Land.builder()
            .name(name)
            .internalName(internalName)
            .size(size)
            .roomType(roomType)
            .latitude(latitude)
            .longitude(longitude)
            .phone(phone)
            .imageUrls(imageUrls)
            .address(address)
            .description(description)
            .floor(floor)
            .deposit(deposit)
            .monthlyFee(monthlyFee)
            .charterFee(charterFee)
            .managementFee(managementFee)
            .optRefrigerator(optRefrigerator != null ? optRefrigerator : false)
            .optCloset(optCloset != null ? optCloset : false)
            .optTv(optTv != null ? optTv : false)
            .optMicrowave(optMicrowave != null ? optMicrowave : false)
            .optGasRange(optGasRange != null ? optGasRange : false)
            .optInduction(optInduction != null ? optInduction : false)
            .optWaterPurifier(optWaterPurifier != null ? optWaterPurifier : false)
            .optAirConditioner(optAirConditioner != null ? optAirConditioner : false)
            .optWasher(optWasher != null ? optWasher : false)
            .build();
    }
}

