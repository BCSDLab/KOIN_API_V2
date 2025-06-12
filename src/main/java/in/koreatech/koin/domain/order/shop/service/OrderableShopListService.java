package in.koreatech.koin.domain.order.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopDetailInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse.ShopOpenInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopListQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderableShopListService {

    private final OrderableShopListQueryRepository orderableShopListQueryRepository;

    public List<OrderableShopsResponse> getOrderableShops(
        OrderableShopsSortCriteria sortCriteria,
        List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount
    ) {
        List<OrderableShopBaseInfo> shopBaseInfo = findAllOrderableShopBaseInfo(filterCriteria, minimumAmount);
        if (shopBaseInfo.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> shopIds = shopBaseInfo.stream().map(OrderableShopBaseInfo::shopId).toList();

        OrderableShopDetailInfo shopDetailInfo = findAllOrderableShopDetailInfo(shopBaseInfo, shopIds);

        return shopBaseInfo.stream()
            .map(baseInfo -> OrderableShopsResponse.of(baseInfo, shopDetailInfo.shopCategories(),
                shopDetailInfo.shopImages(), shopDetailInfo.shopOpens(), shopDetailInfo.shopOpenStatus()))
            .sorted(sortCriteria.getComparator().thenComparing(OrderableShopsResponse::name))
            .collect(Collectors.toList());
    }

    private List<OrderableShopBaseInfo> findAllOrderableShopBaseInfo(List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount) {
        return orderableShopListQueryRepository.findAllOrderableShopInfo(filterCriteria, minimumAmount);
    }

    private OrderableShopDetailInfo findAllOrderableShopDetailInfo(List<OrderableShopBaseInfo> shopBaseInfo,
        List<Integer> shopIds) {
        var shopCategories = findAllShopCategoriesByShopIds(shopIds);
        var shopImages = findAllShopImagesByShopIds(shopIds);
        var shopOpensSchedule = findAllShopOpensByShopIds(shopIds);

        var todayShopOpens = extractTodayOpenSchedule(shopOpensSchedule);
        var shopOpenStatus = extractShopOpenStatus(shopBaseInfo, todayShopOpens);

        return new OrderableShopDetailInfo(shopCategories, shopImages, shopOpensSchedule, shopOpenStatus);
    }

    private Map<Integer, List<Integer>> findAllShopCategoriesByShopIds(List<Integer> shopIds) {
        return orderableShopListQueryRepository.findAllCategoriesByShopIds(shopIds);
    }

    private Map<Integer, List<String>> findAllShopImagesByShopIds(List<Integer> shopIds) {
        return orderableShopListQueryRepository.findAllShopImagesByShopIds(shopIds);
    }

    private Map<Integer, List<ShopOpenInfo>> findAllShopOpensByShopIds(List<Integer> shopIds) {
        return orderableShopListQueryRepository.findAllShopOpensByShopIds(shopIds);
    }

    private Map<Integer, ShopOpenInfo> extractTodayOpenSchedule(Map<Integer, List<ShopOpenInfo>> allShopOpens) {
        String today = LocalDateTime.now().getDayOfWeek().toString();
        Map<Integer, ShopOpenInfo> result = new HashMap<>();

        for (Map.Entry<Integer, List<ShopOpenInfo>> entry : allShopOpens.entrySet()) {
            for (ShopOpenInfo openInfo : entry.getValue()) {
                if (openInfo != null && today.equals(openInfo.dayOfWeek())) {
                    result.put(entry.getKey(), openInfo);
                    break;
                }
            }
        }
        return result;
    }

    private Map<Integer, OrderableShopOpenStatus> extractShopOpenStatus(
        List<OrderableShopBaseInfo> shopBaseInfos,
        Map<Integer, ShopOpenInfo> shopOpens
    ) {
        DayOfWeek dayOfWeekToday = LocalDateTime.now().getDayOfWeek();
        LocalTime nowTime = LocalTime.now();

        return shopBaseInfos.stream()
            .collect(Collectors.toMap(
                OrderableShopBaseInfo::shopId,
                shopInfo -> {
                    ShopOpenInfo shopOpen = shopOpens.getOrDefault(shopInfo.shopId(), null);
                    return shopInfo.determineOpenStatus(shopOpen, dayOfWeekToday, nowTime);
                }
            ));
    }
}
