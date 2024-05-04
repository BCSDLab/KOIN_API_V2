package in.koreatech.koin.admin.land.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.global.validation.UniqueId;
import in.koreatech.koin.global.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminLandsRequest(
    @Schema(description = "이름 - not null - 최대 255자", example = "금실타운", required = true)
    @NotNull(message = "방이름은 필수입니다.")
    @Size(max = 255, message = "방이름의 최대 길이는 255자입니다.")
    String name,

    @Schema(description = "크기", example = "9.0")
    Double size,

    @Schema(description = "종류 - 최대 20자", example = "원룸")
    @Size(max = 20, message = "방종류의 최대 길이는 20자입니다.")
    String roomType,

    @Schema(description = "위도", example = "36.766205")
    Double latitude,

    @Schema(description = "경도", example = "127.284638")
    Double longitude,

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

    // Add additional options as needed
    @Schema(description = "tv 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optTv,

    @Schema(description = "전자레인지 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optMicrowave,

    @Schema(description = "가스레인지 보유 여부 - null일경우 false로 요청됨", example = "false")
    Boolean optGasRange,

    @Schema(description = "인덕션 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optInduction,

    // Continue adding other options following the same pattern
    @Schema(description = "정수기 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optWaterPurifier,

    @Schema(description = "에어컨 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optAirConditioner,

    @Schema(description = "샤워기 보유 여부 - null일경우 false로 요청됨", example = "true")
    Boolean optWasher
) {}


