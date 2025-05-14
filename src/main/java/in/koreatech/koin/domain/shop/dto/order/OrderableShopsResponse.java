package in.koreatech.koin.domain.shop.dto.order;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.repository.order.dto.OrderableShopInfo;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopsResponse(
    Integer shopId,
    String name,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Long reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip,
    Boolean isOpen,
    List<Integer> categoryIds,
    List<String> imageUrls,
    List<ShopOpenInfo> open
) {

    public static OrderableShopsResponse of(
        OrderableShopInfo info,
        Map<Integer, List<Integer>> categoryMap,
        Map<Integer, List<String>> imageMap,
        Map<Integer, List<ShopOpenInfo>> openMap
    ) {
        Integer shopId = info.shopId();
        return new OrderableShopsResponse(
            shopId,
            info.name(),
            info.isDeliveryAvailable(),
            info.isTakeoutAvailable(),
            info.minimumOrderAmount(),
            info.ratingAverage(),
            info.reviewCount(),
            info.minimumDeliveryTip(),
            info.maximumDeliveryTip(),
            info.isOpen(),
            categoryMap.getOrDefault(shopId, new ArrayList<>()),
            imageMap.getOrDefault(shopId, new ArrayList<>()),
            openMap.getOrDefault(shopId, new ArrayList<>())
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record ShopOpenInfo(

        @JsonIgnore
        Integer shopId,
        String dayOfWeek,
        Boolean closed,
        LocalTime openTime,
        LocalTime closeTime
    ) {

    }
}
