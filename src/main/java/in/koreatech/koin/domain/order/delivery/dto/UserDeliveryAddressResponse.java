package in.koreatech.koin.domain.order.delivery.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserDeliveryAddressResponse(
    @Schema(description = "사용자 배달 주소 ID", example = "16")
    Integer userDeliveryAddressId,
    @Schema(description = "전체 주소 (도로명)", example = "충청남도 천안시 동남구 병천면 충절로 1628-17 에듀윌 301호")
    String fullAddress,

    @Schema(description = "배달기사 요청사항", example = "문 앞에 놓아주세요.")
    String toRider
) {

    public static UserDeliveryAddressResponse of(Integer id, String fullAddress, String toRider) {
        return new UserDeliveryAddressResponse(id, fullAddress, toRider);
    }
}
