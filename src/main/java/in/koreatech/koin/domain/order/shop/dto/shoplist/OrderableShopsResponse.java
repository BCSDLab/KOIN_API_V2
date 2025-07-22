package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopsResponse(
    Integer shopId,
    Integer orderableShopId,
    String name,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Boolean serviceEvent,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Long reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip,
    Boolean isOpen,
    List<Integer> categoryIds,
    List<String> imageUrls,
    List<OrderableShopOpenInfo> open,
    OrderableShopOpenStatus openStatus
) {

    public static OrderableShopsResponse of(
        OrderableShopBaseInfo info,
        Map<Integer, List<Integer>> categoryMap,
        Map<Integer, List<String>> imageMap,
        Map<Integer, List<OrderableShopOpenInfo>> openMap,
        Map<Integer, OrderableShopOpenStatus> openStatusMap
    ) {
        Integer shopId = info.shopId();
        return new OrderableShopsResponse(
            info.shopId(),
            info.orderableShopId(),
            info.name(),
            info.isDeliveryAvailable(),
            info.isTakeoutAvailable(),
            info.serviceEvent(),
            info.minimumOrderAmount(),
            info.ratingAverage(),
            info.reviewCount(),
            info.minimumDeliveryTip(),
            info.maximumDeliveryTip(),
            info.isOpen(),
            categoryMap.getOrDefault(shopId, new ArrayList<>()),
            imageMap.getOrDefault(shopId, new ArrayList<>()),
            openMap.getOrDefault(shopId, new ArrayList<>()),
            openStatusMap.getOrDefault(shopId, null)
        );
    }
}
