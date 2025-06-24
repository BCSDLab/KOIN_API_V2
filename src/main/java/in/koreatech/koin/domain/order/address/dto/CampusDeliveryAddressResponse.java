package in.koreatech.koin.domain.order.address.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * record 관련 레디스 캐시 직렬화 문제가 있어서 class 로 구현
 */
@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class CampusDeliveryAddressResponse {

    @Schema(description = "주소 수", example = "1")
    private final Integer count;

    @Schema(description = "주소 목록")
    private final List<InnerCampusDeliveryAddressResponse> addresses;

    @JsonCreator
    public CampusDeliveryAddressResponse(
        @JsonProperty("count") Integer count,
        @JsonProperty("addresses") List<InnerCampusDeliveryAddressResponse> addresses
    ) {
        this.count = count;
        this.addresses = addresses;
    }

    @Getter
    @JsonNaming(SnakeCaseStrategy.class)
    public static class InnerCampusDeliveryAddressResponse {

        @Schema(description = "고유 ID", example = "1")
        private final Integer id;

        @Schema(description = "주소 타입명", example = "기숙사")
        private final String type;

        @Schema(description = "전체 주소", example = "충남 천안시 동남구 병천면 충절로 1600 한국기술교육대학교 제1캠퍼스 생활관 101동")
        private final String fullAddress;

        @Schema(description = "요약 주소", example = "101동(해울)")
        private final String shortAddress;

        @JsonCreator
        public InnerCampusDeliveryAddressResponse(
            @JsonProperty("id") Integer id,
            @JsonProperty("type") String type,
            @JsonProperty("full_address") String fullAddress,
            @JsonProperty("short_address") String shortAddress
        ) {
            this.id = id;
            this.type = type;
            this.fullAddress = fullAddress;
            this.shortAddress = shortAddress;
        }

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
