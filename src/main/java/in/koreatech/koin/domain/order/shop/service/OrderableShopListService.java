package in.koreatech.koin.domain.order.shop.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopDetailInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopListQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderableShopListService {

    private final OrderableShopListQueryRepository orderableShopListQueryRepository;
    private final OrderableShopOpenInfoProcessor orderableShopOpenInfoProcessor;

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
        Map<Integer, List<Integer>> shopCategories =
            orderableShopListQueryRepository.findAllCategoriesByShopIds(shopIds);

        Map<Integer, List<String>> shopImages =
            orderableShopListQueryRepository.findAllShopImagesByShopIds(shopIds);

        Map<Integer, List<OrderableShopOpenInfo>> shopOpensSchedule =
            orderableShopListQueryRepository.findAllShopOpensByShopIds(shopIds);

        var todayShopOpens = orderableShopOpenInfoProcessor.extractTodayOpenSchedule(shopOpensSchedule);
        var shopOpenStatus = orderableShopOpenInfoProcessor.extractShopOpenStatus(shopBaseInfo, todayShopOpens);

        return new OrderableShopDetailInfo(shopCategories, shopImages, shopOpensSchedule, shopOpenStatus);
    }
}
