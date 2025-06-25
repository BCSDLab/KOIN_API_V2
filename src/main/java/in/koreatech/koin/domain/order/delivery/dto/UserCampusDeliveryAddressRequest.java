package in.koreatech.koin.domain.order.delivery.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserCampusDeliveryAddressRequest (
    @Schema(description = "교내 배달 주소 ID", example = "1", requiredMode = REQUIRED)
    @NotNull
    Integer campusDeliveryAddressId,
    @Schema(description = "배달기사 요청사항", example = "문 앞에 놓아주세요.")
    String toRider
) {

}
