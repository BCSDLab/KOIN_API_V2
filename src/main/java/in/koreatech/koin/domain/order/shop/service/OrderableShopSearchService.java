package in.koreatech.koin.domain.order.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultSortCriteria;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopListQueryRepository;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopSearchQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopSearchService {

    private final OrderableShopSearchQueryRepository orderableShopSearchQueryRepository;
    private final OrderableShopListQueryRepository orderableShopListQueryRepository;
    private final OrderableShopOpenInfoProcessor orderableShopOpenInfoProcessor;

    private static final String BLANK = " ";
    private static final String EMPTY = "";

    @Transactional(readOnly = true)
    public OrderableShopSearchRelatedKeywordResponse searchRelatedKeyword(String keyword) {
        String trimmedKeyword = removeBlank(keyword);
        String processedKeyword = HangulCleaner.removeIncompleteHangul(trimmedKeyword);
        var shopNameSearchResult = orderableShopSearchQueryRepository.findAllOrderableShopByKeyword(processedKeyword);
        var menuNameSearchResult = orderableShopSearchQueryRepository.findAllMenuByKeyword(processedKeyword);
        return OrderableShopSearchRelatedKeywordResponse.from(
            keyword, processedKeyword, shopNameSearchResult, menuNameSearchResult
        );
    }

    @Transactional(readOnly = true)
    public OrderableShopSearchResultResponse searchByKeyword(String keyword, OrderableShopSearchResultSortCriteria sortCriteria) {
        String trimmedKeyword = removeBlank(keyword);
        String processedKeyword = HangulCleaner.removeIncompleteHangul(trimmedKeyword);
        var searchAtMenuName = orderableShopSearchQueryRepository.searchOrderableShopsByMenuKeyword(processedKeyword);
        var searchAtShopName = orderableShopSearchQueryRepository.searchOrderableShopsByShopNameKeyword(processedKeyword);

        List<OrderableShopBaseInfo> shopBaseInfo = combinedShopBaseInfo(searchAtMenuName, searchAtShopName);
        if(shopBaseInfo.isEmpty()) {
            return OrderableShopSearchResultResponse.empty(keyword, processedKeyword);
        }

        List<Integer> orderableShopIds = shopBaseInfo.stream().map(OrderableShopBaseInfo::orderableShopId).toList();
        Map<Integer, String> orderableShopThumbnailImageMap = orderableShopSearchQueryRepository.findOrderableShopThumbnailImageByOrderableShopIds(
            orderableShopIds);
        Map<Integer, List<String>> containMenuNameMap = orderableShopSearchQueryRepository.findOrderableShopContainMenuNameByOrderableShopIds(
            orderableShopIds, processedKeyword);

        List<Integer> shopIds = shopBaseInfo.stream().map(OrderableShopBaseInfo::shopId).toList();
        Map<Integer, List<OrderableShopOpenInfo>> orderableShopOpenInfoMap =
            orderableShopListQueryRepository.findAllShopOpensByShopIds(shopIds);
        Map<Integer, OrderableShopOpenInfo> todayShopOpens =
            orderableShopOpenInfoProcessor.extractTodayOpenSchedule(orderableShopOpenInfoMap);
        Map<Integer, OrderableShopOpenStatus> shopOpenStatusMap =
            orderableShopOpenInfoProcessor.extractShopOpenStatus(shopBaseInfo, todayShopOpens);

        return OrderableShopSearchResultResponse.from(
            keyword, processedKeyword, shopBaseInfo, orderableShopThumbnailImageMap,
            shopOpenStatusMap, containMenuNameMap, sortCriteria
        );
    }

    private List<OrderableShopBaseInfo> combinedShopBaseInfo(
        List<OrderableShopBaseInfo> searchAtMenuName, List<OrderableShopBaseInfo> searchAtShopName
    ) {
        return new ArrayList<>(Stream.concat(
                searchAtMenuName.stream(),
                searchAtShopName.stream()
            )
            .collect(Collectors.toMap(
                OrderableShopBaseInfo::shopId,
                Function.identity(),
                (existing, replacement) -> existing
            ))
            .values());
    }

    private String removeBlank(String query) {
        return query.replaceAll(BLANK, EMPTY);
    }
}
