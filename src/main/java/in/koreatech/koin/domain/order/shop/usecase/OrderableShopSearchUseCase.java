package in.koreatech.koin.domain.order.shop.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse.OrderableShopSearchResult;
import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultSortCriteria;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import in.koreatech.koin.domain.order.shop.service.OrderableShopSearchService;
import in.koreatech.koin.domain.order.shop.service.SearchKeywordProcessor;
import in.koreatech.koin.domain.order.shop.service.ShopOpenScheduleService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopSearchUseCase {

    private final OrderableShopSearchService orderableShopSearchService;
    private final ShopOpenScheduleService shopOpenScheduleService;
    private final SearchKeywordProcessor searchKeywordProcessor;

    public OrderableShopSearchRelatedKeywordResponse searchRelatedKeyword(String rawKeyword) {
        String processedKeyword = searchKeywordProcessor.process(rawKeyword);

        var shopNameResults = orderableShopSearchService.findShopNamesByKeyword(processedKeyword);
        var menuNameResults = orderableShopSearchService.findMenuNamesByKeyword(processedKeyword);

        return OrderableShopSearchRelatedKeywordResponse.from(
            rawKeyword, processedKeyword, shopNameResults, menuNameResults
        );
    }

    public OrderableShopSearchResultResponse searchByKeyword(
        String rawKeyword,
        OrderableShopSearchResultSortCriteria sortCriteria
    ) {
        List<String> processedKeywords = searchKeywordProcessor.processToKeywords(rawKeyword);
        if (processedKeywords.isEmpty()) {
            return OrderableShopSearchResultResponse.empty(rawKeyword, processedKeywords);
        }

        List<OrderableShopBaseInfo> shopBaseInfos = orderableShopSearchService.findOrderableShopsByKeywords(
            processedKeywords);
        if (shopBaseInfos.isEmpty()) {
            return OrderableShopSearchResultResponse.empty(rawKeyword, processedKeywords);
        }

        OrderableShopSearchResultDetails shopDetailInfos = collectSearchResultDetailInfo(shopBaseInfos,
            processedKeywords);

        var combineAndSortSearchResults = combineSearchResults(shopBaseInfos, shopDetailInfos)
            .stream()
            .sorted(sortCriteria.getComparator().thenComparing(OrderableShopSearchResult::name))
            .toList();
        return OrderableShopSearchResultResponse.from(rawKeyword, processedKeywords, combineAndSortSearchResults);
    }

    private OrderableShopSearchResultDetails collectSearchResultDetailInfo(
        List<OrderableShopBaseInfo> shops,
        List<String> processedKeywords
    ) {
        List<Integer> orderableShopIds = shops.stream().map(OrderableShopBaseInfo::orderableShopId).toList();

        Map<Integer, String> thumbnailImageMap = orderableShopSearchService.findThumbnailUrlsByOrderableShopIds(
            orderableShopIds);
        Map<Integer, List<String>> containMenuNameMap = orderableShopSearchService.findMatchingMenuNamesByOrderableShopIds(
            orderableShopIds, processedKeywords);
        Map<Integer, OrderableShopOpenStatus> openStatusMap = shopOpenScheduleService.determineShopOpenStatuses(shops);

        return new OrderableShopSearchResultDetails(thumbnailImageMap, containMenuNameMap, openStatusMap);
    }

    private List<OrderableShopSearchResult> combineSearchResults(
        List<OrderableShopBaseInfo> shopBaseInfos,
        OrderableShopSearchResultDetails shopDetailInfos
    ) {
        return shopBaseInfos.stream().map(it -> OrderableShopSearchResult.from(
                it,
                shopDetailInfos.thumbnailImageByOrderableShopId.getOrDefault(it.orderableShopId(), null),
                shopDetailInfos.openStatusByShopId.getOrDefault(it.shopId(), null),
                shopDetailInfos.containMenuNamesByOrderableShopId.getOrDefault(it.orderableShopId(),
                    Collections.emptyList())
            )
        ).toList();
    }

    private record OrderableShopSearchResultDetails(
        Map<Integer, String> thumbnailImageByOrderableShopId,
        Map<Integer, List<String>> containMenuNamesByOrderableShopId,
        Map<Integer, OrderableShopOpenStatus> openStatusByShopId
    ) {
    }
}
