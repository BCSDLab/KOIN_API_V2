package in.koreatech.koin.domain.order.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;

@Component
public class OrderableShopOpenInfoProcessor {

    public Map<Integer, OrderableShopOpenInfo> extractTodayOpenSchedule(Map<Integer, List<OrderableShopOpenInfo>> allShopOpens) {
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

    public Map<Integer, OrderableShopOpenStatus> extractShopOpenStatus(
        List<OrderableShopBaseInfo> shopBaseInfos,
        Map<Integer, OrderableShopOpenInfo> shopOpens
    ) {
        DayOfWeek dayOfWeekToday = LocalDateTime.now().getDayOfWeek();
        LocalTime nowTime = LocalTime.now();

        return shopBaseInfos.stream()
            .collect(Collectors.toMap(
                OrderableShopBaseInfo::shopId,
                shopInfo -> {
                    OrderableShopOpenInfo shopOpen = shopOpens.getOrDefault(shopInfo.shopId(), null);
                    return shopInfo.determineOpenStatus(shopOpen, dayOfWeekToday, nowTime);
                }
            ));
    }
}
