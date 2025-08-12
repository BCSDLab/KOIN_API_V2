package in.koreatech.koin.domain.order.shop.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopCategoryFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopDetailInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopImageInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopListQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderableShopListService {

    private final OrderableShopListQueryRepository orderableShopListQueryRepository;
    private final OrderableShopOpenStatusEvaluator orderableShopOpenStatusEvaluator;

    public List<OrderableShopsResponse> getOrderableShops(
        OrderableShopsSortCriteria sortCriteria,
        List<OrderableShopsFilterCriteria> filterCriteria,
        OrderableShopCategoryFilterCriteria categoryFilterCriteria,
        Integer minimumAmount
    ) {
        List<OrderableShopBaseInfo> shopBaseInfo = orderableShopListQueryRepository.findAllOrderableShopInfo(
            filterCriteria, categoryFilterCriteria, minimumAmount
        );
        if (shopBaseInfo.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> shopIds = shopBaseInfo.stream().map(OrderableShopBaseInfo::shopId).toList();

        OrderableShopDetailInfo shopDetailInfo = findAllOrderableShopDetailInfo(shopBaseInfo, shopIds);

        return shopBaseInfo.stream()
            .map(baseInfo -> OrderableShopsResponse.of(baseInfo, shopDetailInfo.shopCategories(),
                shopDetailInfo.shopImages(), shopDetailInfo.shopOpenStatus()))
            .sorted(sortCriteria.getComparator().thenComparing(OrderableShopsResponse::name))
            .collect(Collectors.toList());
    }

    private OrderableShopDetailInfo findAllOrderableShopDetailInfo(List<OrderableShopBaseInfo> shopBaseInfo,
        List<Integer> shopIds) {
        Map<Integer, List<Integer>> shopCategories =
            orderableShopListQueryRepository.findAllCategoriesByShopIds(shopIds);

        List<Integer> orderableShopIds = shopBaseInfo.stream().map(OrderableShopBaseInfo::orderableShopId).toList();
        Map<Integer, List<OrderableShopImageInfo>> shopImages =
            orderableShopListQueryRepository.findAllOrderableShopImagesByOrderableShopIds(orderableShopIds);

        Map<Integer, OrderableShopOpenStatus> shopOpenStatus =
            orderableShopOpenStatusEvaluator.findOpenStatusByShopBasicInfos(shopBaseInfo);

        return new OrderableShopDetailInfo(shopCategories, shopImages, shopOpenStatus);
    }
}
