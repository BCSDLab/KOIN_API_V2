package in.koreatech.koin.domain.order.address.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CampusDeliveryAddressResponse(

    @Schema(description = "주소 수", example = "1")
    Integer count,
    @Schema(description = "주소 목록")
    List<InnerCampusDeliveryAddressResponse> addresses
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCampusDeliveryAddressResponse (
        @Schema(description = "고유 ID", example = "1")
        Integer id,

        @Schema(description = "주소 타입명", example = "기숙사")
        String type,

        @Schema(description = "전체 주소", example = "충남 천안시 동남구 병천면 충절로 1600 한국기술교육대학교 제1캠퍼스 생활관 101동")
        String fullAddress,

        @Schema(description = "요약 주소", example = "101동(해울)")
        String shortAddress
    ) {

        public static InnerCampusDeliveryAddressResponse from(CampusDeliveryAddress address) {
            return new InnerCampusDeliveryAddressResponse(
                address.getId(),
                address.getCampusAddressType().getName(),
                address.getFullAddress(),
                address.getShortAddress()
            );
        }
    }

    public static CampusDeliveryAddressResponse from(List<CampusDeliveryAddress> campusDeliveryAddresses) {

        List<InnerCampusDeliveryAddressResponse> addresses = campusDeliveryAddresses.stream()
            .map(InnerCampusDeliveryAddressResponse::from)
            .toList();

        return new CampusDeliveryAddressResponse(
            addresses.size(),
            addresses
        );
    }
}
