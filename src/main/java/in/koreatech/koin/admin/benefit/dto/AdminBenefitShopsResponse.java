package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBenefitShopsResponse(
    @Schema(example = "3", description = "상점 개수")
    Integer count,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {

    public static AdminBenefitShopsResponse from(
        List<InnerShopResponse> shops
    ) {
        return new AdminBenefitShopsResponse(shops.size(), shops);
    }

    public record InnerShopResponse(
        @Schema(example = "1", description = "고유 id")
        Integer id,

        @Schema(example = "수신반점", description = "이름")
        String name
    ) {

        public static InnerShopResponse from(InnerShopResponse shop) {
            return new InnerShopResponse(
                shop.id(),
                shop.name()
            );
        }
    }
}
