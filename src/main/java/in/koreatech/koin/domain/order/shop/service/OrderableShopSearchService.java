package in.koreatech.koin.domain.order.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopMenuNameKeywordHit;
import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopNameKeywordHit;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopSearchQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopSearchService {

    private final OrderableShopSearchQueryRepository orderableShopSearchQueryRepository;

    public List<OrderableShopNameKeywordHit> findShopNamesByKeyword(List<String> processedKeywords) {
        return orderableShopSearchQueryRepository.findAllOrderableShopByKeyword(processedKeywords);
    }

    public List<OrderableShopMenuNameKeywordHit> findMenuNamesByKeyword(List<String> processedKeywords) {
        return orderableShopSearchQueryRepository.findAllMenuByKeyword(processedKeywords);
    }

    public List<OrderableShopBaseInfo> findOrderableShopsByKeywords(List<String> processedKeywords) {
        var searchAtMenuName = orderableShopSearchQueryRepository.searchOrderableShopsByMenuKeyword(processedKeywords);
        var searchAtShopName = orderableShopSearchQueryRepository.searchOrderableShopsByShopNameKeyword(
            processedKeywords);
        return combineAndDeduplicateShopBaseInfo(searchAtMenuName, searchAtShopName);
    }

    public Map<Integer, String> findThumbnailUrlsByOrderableShopIds(List<Integer> orderableShopIds) {
        return orderableShopSearchQueryRepository.findOrderableShopThumbnailImageByOrderableShopIds(orderableShopIds);
    }

    public Map<Integer, List<String>> findMatchingMenuNamesByOrderableShopIds(List<Integer> orderableShopIds,
        List<String> processedKeywords) {
        return orderableShopSearchQueryRepository.findOrderableShopContainMenuNameByOrderableShopIds(orderableShopIds,
            processedKeywords);
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
