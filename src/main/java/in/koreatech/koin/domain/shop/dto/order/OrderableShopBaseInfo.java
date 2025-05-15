package in.koreatech.koin.domain.shop.dto.order;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse.ShopOpenInfo;

public record OrderableShopBaseInfo(
    Integer shopId,
    String name,
    Boolean isDeliveryAvailable,
    Boolean isTakeoutAvailable,
    Integer minimumOrderAmount,
    Double ratingAverage,
    Long reviewCount,
    Integer minimumDeliveryTip,
    Integer maximumDeliveryTip,
    Boolean isOpen
) {

    /**
     * 현재 요일과 시간을 고려 하여 상점의 영업 상태를 결정.
     * OrderableShopOpenStatus.OPERATING: 사장님이 영업 중 상태를 설정. shop_operation 테이블의 is_open 컬럼이 true면 OPERATING
     * OrderableShopOpenStatus.PREPARING: shop_opens 테이블 의 가게 영업 스케줄에 의하면 현재 시간은 가게의 영업 시간에 포함 되지만
     *                                    사장님이 영업 중 상태를 설정하지 않음.
     * OrderableShopOpenStatus.CLOSED: 사장님이 영업 중 상태를 설정 하지 않았으며, 가게의 원래 영업 시간에도 포함 되지 않는 시간
     */
    public OrderableShopOpenStatus determineOpenStatus(
        ShopOpenInfo shopOpenInfos,
        DayOfWeek currentDayOfWeek,
        LocalTime currentTime
    ) {
        if (this.isOpen()) {
            return OrderableShopOpenStatus.OPERATING;
        }

        if (shopOpenInfos.isScheduledToOpenAt(currentDayOfWeek, currentTime)) {
            return OrderableShopOpenStatus.PREPARING;
        } else {
            return OrderableShopOpenStatus.CLOSED;
        }
    }
}
