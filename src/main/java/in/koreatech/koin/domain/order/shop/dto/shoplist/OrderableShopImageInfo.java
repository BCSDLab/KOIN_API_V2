package in.koreatech.koin.domain.order.shop.dto.shoplist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopImageInfo(
    @JsonIgnore
    Integer orderableShopId,
    String imageUrl,
    Boolean isThumbnail
) {
}
