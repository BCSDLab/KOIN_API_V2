package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBenefitShopsResponse(
    @Schema(example = "3", description = "상점 개수")
    Integer count,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {

    public static AdminBenefitShopsResponse from(List<Shop> shops) {
        return new AdminBenefitShopsResponse(
            shops.size(),
            shops.stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerShopResponse(
        @Schema(example = "1", description = "고유 id")
        Integer id,

        @Schema(example = "수신반점", description = "이름")
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
