package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminSearchBenefitShopsResponse(
    @Schema(description = "혜택 상점 리스트")
    List<InnerShopResponse> benefitShops,

    @Schema(description = "혜택을 제공하지 않는 상점 리스트")
    List<InnerShopResponse> nonBenefitShops
) {

    public static AdminSearchBenefitShopsResponse from(List<Shop> benefitShops, List<Shop> nonBenefitShops) {
        return new AdminSearchBenefitShopsResponse(
            benefitShops.stream()
                .map(InnerShopResponse::from)
                .toList(),
            nonBenefitShops.stream().
                map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerShopResponse(
        @Schema(description = "상점 ID", example = "1")
        Integer id,

        @Schema(description = "상점 이름", example = "수신반점")
        String name
    ) {

        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(
                shop.getId(),
                shop.getName()
            );
        }
    }
}
