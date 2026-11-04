package in.koreatech.koin.admin.land.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.land.model.Land;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminLandRequest(
    @Schema(description = "이름", example = "금실타운", requiredMode = REQUIRED)
    @NotBlank(message = "방이름은 필수입니다.")
    @Size(max = 255, message = "방이름의 최대 길이는 255자입니다.")
    String name,

    @Schema(description = "이름", example = "금실타운")
    @Size(max = 50, message = "방이름의 최대 길이는 50자입니다.")
    String internalName,

    @Schema(description = "크기 - 비워 보내면 비워짐", example = "9.0")
    Double size,

    @Schema(description = "종류 - 최대 20자, 비워 보내면 비워짐", example = "원룸")
    @Size(max = 20, message = "방종류의 최대 길이는 20자입니다.")
    String roomType,

    @Schema(description = "위도 - 국내 범위(33~39)", example = "36.766205", requiredMode = REQUIRED)
    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "33.0", message = "위도는 33 이상이어야 합니다.")
    @DecimalMax(value = "39.0", message = "위도는 39 이하여야 합니다.")
    Double latitude,

    @Schema(description = "경도 - 국내 범위(124~132)", example = "127.284638", requiredMode = REQUIRED)
    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "124.0", message = "경도는 124 이상이어야 합니다.")
    @DecimalMax(value = "132.0", message = "경도는 132 이하여야 합니다.")
    Double longitude,

    @Schema(description = "전화번호 - 정규식 `^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$` 을 만족해야함, null로 보내면 비워짐", example = "041-111-1111")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$", message = "전화번호의 형식이 올바르지 않습니다.")
    String phone,

    @Schema(description = "이미지 url 리스트 - 빈 리스트로 보내면 비워짐, 비어 있는 주소는 넣을 수 없음")
    List<@NotBlank(message = "이미지 주소는 비어 있을 수 없습니다.") String> imageUrls,

    @Schema(description = "주소 - 최대 65535자", example = "충청남도 천안시 동남구 병천면", requiredMode = REQUIRED)
    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 65535, message = "주소의 최대 길이는 65535자입니다.")
    String address,

    @Schema(description = "설명 - 최대 65535자, 수정 시 null이면 기존 값 유지", example = "1년 계약시 20만원 할인")
    @Size(max = 65535, message = "설명의 최대 길이는 65535자입니다.")
    String description,

    @Schema(description = "층수 - 음수 불가능, 수정 시 null이면 기존 값 유지", example = "4")
    @PositiveOrZero(message = "층수는 0 이상이어야합니다.")
    Integer floor,

    @Schema(description = "보증금 - 최대 255자, 수정 시 null이면 기존 값 유지", example = "30")
    @Size(max = 255, message = "보증금의 최대 길이는 255자입니다.")
    String deposit,

    @Schema(description = "월세 - 최대 255자, 수정 시 null이면 기존 값 유지", example = "200만원 (6개월)")
    @Size(max = 255, message = "월세의 최대 길이는 255자입니다.")
    String monthlyFee,

    @Schema(description = "전세 - 최대 20자, 수정 시 null이면 기존 값 유지", example = "3500")
    @Size(max = 20, message = "전세의 최대 길이는 20자입니다.")
    String charterFee,

    @Schema(description = "관리비 - 최대 255자, 수정 시 null이면 기존 값 유지", example = "21(1인 기준)")
    @Size(max = 255, message = "관리비의 최대 길이는 255자입니다.")
    String managementFee,

    @Schema(description = "냉장고 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optRefrigerator,

    @Schema(description = "옷장 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optCloset,

    @Schema(description = "tv 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optTv,

    @Schema(description = "전자레인지 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optMicrowave,

    @Schema(description = "가스레인지 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "false")
    Boolean optGasRange,

    @Schema(description = "인덕션 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optInduction,

    @Schema(description = "정수기 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optWaterPurifier,

    @Schema(description = "에어컨 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optAirConditioner,

    @Schema(description = "샤워기 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optWasher,

    @Schema(description = "침대 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "false")
    Boolean optBed,

    @Schema(description = "책상 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optDesk,

    @Schema(description = "신발장 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optShoeCloset,

    @Schema(description = "전자 도어락 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optElectronicDoorLocks,

    @Schema(description = "비데 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "false")
    Boolean optBidet,

    @Schema(description = "베란다 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "false")
    Boolean optVeranda,

    @Schema(description = "엘리베이터 보유 여부 - 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean optElevator,

    @Schema(description = "월세 매물 여부 - 학생 목록의 월세 배지, 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "true")
    Boolean isMonthlyAvailable,

    @Schema(description = "전세 매물 여부 - 학생 목록의 전세 배지, 등록 시 null이면 false, 수정 시 null이면 기존 값 유지", example = "false")
    Boolean isCharterAvailable
) {
    public Land toLand() {
        return Land.builder()
            .name(name)
            .internalName(Land.toInternalName(name))
            .size(toStringOrNull(size))
            .roomType(roomType)
            .latitude(String.valueOf(latitude))
            .longitude(String.valueOf(longitude))
            .phone(phone)
            .imageUrls(imageUrls)
            .address(address)
            .description(description)
            .floor(floor)
            .deposit(deposit)
            .monthlyFee(monthlyFee)
            .charterFee(charterFee)
            .managementFee(managementFee)
            .optRefrigerator(Boolean.TRUE.equals(optRefrigerator))
            .optCloset(Boolean.TRUE.equals(optCloset))
            .optTv(Boolean.TRUE.equals(optTv))
            .optMicrowave(Boolean.TRUE.equals(optMicrowave))
            .optGasRange(Boolean.TRUE.equals(optGasRange))
            .optInduction(Boolean.TRUE.equals(optInduction))
            .optWaterPurifier(Boolean.TRUE.equals(optWaterPurifier))
            .optAirConditioner(Boolean.TRUE.equals(optAirConditioner))
            .optWasher(Boolean.TRUE.equals(optWasher))
            .optBed(Boolean.TRUE.equals(optBed))
            .optDesk(Boolean.TRUE.equals(optDesk))
            .optShoeCloset(Boolean.TRUE.equals(optShoeCloset))
            .optElectronicDoorLocks(Boolean.TRUE.equals(optElectronicDoorLocks))
            .optBidet(Boolean.TRUE.equals(optBidet))
            .optVeranda(Boolean.TRUE.equals(optVeranda))
            .optElevator(Boolean.TRUE.equals(optElevator))
            .isMonthlyAvailable(Boolean.TRUE.equals(isMonthlyAvailable))
            .isCharterAvailable(Boolean.TRUE.equals(isCharterAvailable))
            .build();
    }

    private static String toStringOrNull(Double value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}

