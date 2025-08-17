package in.koreatech.koin.domain.order.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopListQueryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderableShopOpenStatusEvaluator {

    private final OrderableShopListQueryRepository orderableShopListQueryRepository;

    public Map<Integer, OrderableShopOpenStatus> findOpenStatusByShopBasicInfos(List<OrderableShopBaseInfo> shopBaseInfos) {
        if (shopBaseInfos == null || shopBaseInfos.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> shopIds = shopBaseInfos.stream().map(OrderableShopBaseInfo::shopId).toList();

        // 가게들의 모든 영업 스케줄 정보를 가져오기
        Map<Integer, List<OrderableShopOpenInfo>> allShopOpens =
            orderableShopListQueryRepository.findAllShopOpensByShopIds(shopIds);

        // 오늘(요일 기준) 영업 스케줄만 추출
        Map<Integer, OrderableShopOpenInfo> todayShopOpens = findTodayOpenSchedules(allShopOpens);

        // 최종 영업 상태를 결정
        DayOfWeek dayOfWeekToday = LocalDateTime.now().getDayOfWeek();
        LocalTime nowTime = LocalTime.now();

        return shopBaseInfos.stream()
            .collect(Collectors.toMap(
                OrderableShopBaseInfo::shopId,
                shopInfo -> {
                    OrderableShopOpenInfo todayOpenInfo = todayShopOpens.get(shopInfo.shopId());
                    return determineOpenStatus(todayOpenInfo, dayOfWeekToday, nowTime, shopInfo.isOpen());
                }
            ));
    }

    private Map<Integer, OrderableShopOpenInfo> findTodayOpenSchedules(Map<Integer, List<OrderableShopOpenInfo>> allShopOpens) {
        String today = LocalDateTime.now().getDayOfWeek().toString();
        Map<Integer, OrderableShopOpenInfo> result = new HashMap<>();

        for (Map.Entry<Integer, List<OrderableShopOpenInfo>> entry : allShopOpens.entrySet()) {
            for (OrderableShopOpenInfo openInfo : entry.getValue()) {
                if (openInfo != null && today.equals(openInfo.dayOfWeek())) {
                    result.put(entry.getKey(), openInfo);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 현재 요일과 시간을 고려 하여 상점의 영업 상태를 결정.
     * OrderableShopOpenStatus.OPERATING: 사장님이 영업 중 상태를 설정. shop_operation 테이블의 is_open 컬럼이 true면 OPERATING
     * OrderableShopOpenStatus.PREPARING: shop_opens 테이블 의 가게 영업 스케줄에 의하면 현재 시간은 가게의 영업 시간에 포함 되지만
     *                                    사장님이 영업 중 상태를 설정하지 않음.
     * OrderableShopOpenStatus.CLOSED: 사장님이 영업 중 상태를 설정 하지 않았으며, 가게의 원래 영업 시간에도 포함 되지 않는 시간
     */
    public OrderableShopOpenStatus determineOpenStatus(
        OrderableShopOpenInfo shopOpenInfo,
        DayOfWeek currentDayOfWeek,
        LocalTime currentTime,
        Boolean shopIsOpen
    ) {
        if (shopIsOpen) {
            return OrderableShopOpenStatus.OPERATING;
        }

        if (shopOpenInfo.isScheduledToOpenAt(currentDayOfWeek, currentTime)) {
            return OrderableShopOpenStatus.PREPARING;
        } else {
            return OrderableShopOpenStatus.CLOSED;
        }
    }
}
