package in.koreatech.koin.domain.order.delivery.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserOffCampusDeliveryAddressRequest(

    @Schema(description = "우편 번호", example = "31253", requiredMode = REQUIRED)
    @NotNull(message = "우편번호는 필수입니다.")
    String zipNumber,

    @Schema(description = "시/도", example = "충청남도")
    String siDo,

    @Schema(description = "시/군/구", example = "천안시 동남구")
    String siGunGu,

    @Schema(description = "읍/면/동", example = "병천면")
    String eupMyeonDong,

    @Schema(description = "도로명", example = "충절로")
    String road,

    @Schema(description = "건물명", example = " ")
    String building,

    @Schema(description = "상세 주소", example = "에듀윌 301호")
    @NotBlank(message = "상세 주소는 필수입니다.")
    String detailAddress,

    @Schema(description = "전체 주소 (도로명)", example = "충청남도 천안시 동남구 병천면 충절로 1628-17 에듀윌 301호")
    @NotBlank(message = "전체 주소는 필수입니다.")
    String fullAddress
) {

    public OffCampusDeliveryAddress toOffCampusAddress() {
        return OffCampusDeliveryAddress.builder()
            .zipNumber(zipNumber)
            .siDo(siDo)
            .siGunGu(siGunGu)
            .eupMyeonDong(eupMyeonDong)
            .road(road)
            .building(building)
            .detailAddress(detailAddress)
            .fullAddress(fullAddress)
            .build();
    }
}
