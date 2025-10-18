package in.koreatech.koin.domain.order.delivery.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserOffCampusDeliveryAddressValidateRequest(
    @Schema(description = "시/도", example = "충청남도")
    @NotBlank(message = "시/도는 필수입니다.")
    String siDo,

    @Schema(description = "시/군/구", example = "천안시 동남구")
    @NotBlank(message = "시/군/구는 필수입니다.")
    String siGunGu,

    @Schema(description = "읍/면/동", example = "병천면", nullable = true)
    String eupMyeonDong,

    @Schema(description = "건물 이름", example = "한국기술교육대학교", nullable = true)
    String building

) {

    public OffCampusDeliveryAddress toOffCampusAddress() {
        return OffCampusDeliveryAddress.builder()
            .siDo(siDo)
            .siGunGu(siGunGu)
            .eupMyeonDong(eupMyeonDong)
            .building(building)
            .build();
    }
}
