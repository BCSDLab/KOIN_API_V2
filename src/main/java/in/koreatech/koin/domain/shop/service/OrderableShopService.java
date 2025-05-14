package in.koreatech.koin.domain.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.order.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse.ShopOpenInfo;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsSortCriteria;
import in.koreatech.koin.domain.shop.repository.order.OrderableShopCustomRepository;
import in.koreatech.koin.domain.shop.repository.order.dto.OrderableShopInfo;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderableShopService {

    private final OrderableShopCustomRepository orderableShopCustomRepository;

    public List<OrderableShopsResponse> getOrderableShops(
        OrderableShopsSortCriteria sortCriteria,
        List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount
    ) {
        List<OrderableShopInfo> shopBaseInfo = findAllOrderableShopBaseInfo(filterCriteria, minimumAmount);
        if (shopBaseInfo.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> shopIds = shopBaseInfo.stream().map(OrderableShopInfo::shopId).toList();

        var shopCategories = findAllShopCategoriesByShopIds(shopIds);
        var shopImages = findAllShopImagesByShopIds(shopIds);
        var shopOpens = findAllShopOpensByShopIds(shopIds);

        return shopBaseInfo.stream()
            .map(baseInfo -> OrderableShopsResponse.of(baseInfo, shopCategories, shopImages, shopOpens))
            .sorted(sortCriteria.getComparator())
            .collect(Collectors.toList());
    }

    private List<OrderableShopInfo> findAllOrderableShopBaseInfo(List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount) {
        return orderableShopCustomRepository.findAllOrderableShopInfo(filterCriteria, minimumAmount);
    }

    private Map<Integer, List<Integer>> findAllShopCategoriesByShopIds(List<Integer> shopIds) {
        return orderableShopCustomRepository.findAllCategoriesByShopIds(shopIds);
    }

    private Map<Integer, List<String>> findAllShopImagesByShopIds(List<Integer> shopIds) {
        return orderableShopCustomRepository.findAllShopImagesByShopIds(shopIds);
    }

    private Map<Integer, List<ShopOpenInfo>> findAllShopOpensByShopIds(List<Integer> shopIds) {
        return orderableShopCustomRepository.findAllShopOpensByShopIds(shopIds);
    }
}
