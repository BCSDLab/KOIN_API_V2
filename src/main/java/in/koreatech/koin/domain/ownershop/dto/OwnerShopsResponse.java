package in.koreatech.koin.domain.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerShopsResponse(
    @Schema(description = "매장 수", example = "3", requiredMode = REQUIRED)
    Integer count,

    @Schema(description = "매장 목록")
    List<InnerShopResponse> shops
) {

    public static OwnerShopsResponse from(List<InnerShopResponse> shops) {
        return new OwnerShopsResponse(shops.size(), shops);
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopResponse(
        @Schema(description = "매장 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "매장 이름", example = "감성떡볶이", requiredMode = REQUIRED)
        String name,

        @Schema(description = "이벤트 진행 여부", example = "true", requiredMode = REQUIRED)
        boolean isEvent
    ) {

        public static InnerShopResponse from(Shop shop, boolean isEvent) {
            return new InnerShopResponse(shop.getId(), shop.getName(), isEvent);
        }
    }
}
