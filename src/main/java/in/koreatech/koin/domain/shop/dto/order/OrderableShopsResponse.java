package in.koreatech.koin.domain.shop.dto.order;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
    List<ShopOpenInfo> open,
    OrderableShopOpenStatus openStatus
) {

    public static OrderableShopsResponse of(
        OrderableShopBaseInfo info,
        Map<Integer, List<Integer>> categoryMap,
        Map<Integer, List<String>> imageMap,
        Map<Integer, List<ShopOpenInfo>> openMap,
        Map<Integer, OrderableShopOpenStatus> openStatusMap
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
            openMap.getOrDefault(shopId, new ArrayList<>()),
            openStatusMap.getOrDefault(shopId, null)
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

        public Boolean isScheduledToOpenAt(DayOfWeek currentDayOfWeek, LocalTime currentTime) {
            if (this.closed() || this.openTime == null || this.closeTime == null) {
                return false;
            }

            DayOfWeek scheduledDay = DayOfWeek.valueOf(this.dayOfWeek().toUpperCase());

            return scheduledDay.equals(currentDayOfWeek) &&
                !currentTime.isBefore(this.openTime()) &&
                currentTime.isBefore(this.closeTime());
        }
    }
}
