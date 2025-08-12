package in.koreatech.koin.domain.order.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.model.readmodel.MenuNameKeywordHit;
import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerMenuNameSearchRelatedKeywordResult;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerShopNameSearchRelatedKeywordResult;
import in.koreatech.koin.domain.order.shop.model.readmodel.ShopNameKeywordHit;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopSearchQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderableShopSearchService {

    private final OrderableShopSearchQueryRepository orderableShopSearchQueryRepository;

    public List<ShopNameKeywordHit> findShopNamesByKeyword(String processedKeyword) {
        return orderableShopSearchQueryRepository.findAllOrderableShopByKeyword(processedKeyword);
    }

    public List<MenuNameKeywordHit> findMenuNamesByKeyword(String processedKeyword) {
        return orderableShopSearchQueryRepository.findAllMenuByKeyword(processedKeyword);
    }

    public List<OrderableShopBaseInfo> findOrderableShopsByKeyword(String processedKeyword) {
        var searchAtMenuName = orderableShopSearchQueryRepository.searchOrderableShopsByMenuKeyword(processedKeyword);
        var searchAtShopName = orderableShopSearchQueryRepository.searchOrderableShopsByShopNameKeyword(processedKeyword);
        return combineAndDeduplicateShopBaseInfo(searchAtMenuName, searchAtShopName);
    }

    public Map<Integer, String> findThumbnailUrlsByOrderableShopIds(List<Integer> orderableShopIds) {
        return orderableShopSearchQueryRepository.findOrderableShopThumbnailImageByOrderableShopIds(orderableShopIds);
    }

    public Map<Integer, List<String>> findMatchingMenuNamesByOrderableShopIds(List<Integer> orderableShopIds, String processedKeyword) {
        return orderableShopSearchQueryRepository.findOrderableShopContainMenuNameByOrderableShopIds(orderableShopIds, processedKeyword);
    }

    private List<OrderableShopBaseInfo> combineAndDeduplicateShopBaseInfo(
        List<OrderableShopBaseInfo> fromMenuSearch, List<OrderableShopBaseInfo> fromShopNameSearch
    ) {
        return new ArrayList<>(Stream.concat(fromMenuSearch.stream(), fromShopNameSearch.stream())
            .collect(Collectors.toMap(
                OrderableShopBaseInfo::shopId,
                Function.identity(),
                (existing, replacement) -> existing
            ))
            .values());
    }
}
