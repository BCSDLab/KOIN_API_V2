package in.koreatech.koin.domain.order.shop.usecase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
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

    public OrderableShopSearchResultResponse searchByKeyword(String rawKeyword, OrderableShopSearchResultSortCriteria sortCriteria) {
        String processedKeyword = searchKeywordProcessor.process(rawKeyword);

        List<OrderableShopBaseInfo> shopBaseInfos = orderableShopSearchService.findOrderableShopsByKeyword(processedKeyword);

        if (shopBaseInfos.isEmpty()) {
            return OrderableShopSearchResultResponse.empty(rawKeyword, processedKeyword);
        }

        DetailInfo detailInfo = fetchEnrichmentDataForShops(shopBaseInfos, processedKeyword);

        return OrderableShopSearchResultResponse.from(
            rawKeyword,
            processedKeyword,
            shopBaseInfos,
            detailInfo.thumbnailImageMap(),
            detailInfo.openStatusMap(),
            detailInfo.containMenuNameMap(),
            sortCriteria
        );
    }

    private DetailInfo fetchEnrichmentDataForShops(List<OrderableShopBaseInfo> shops, String processedKeyword) {
        List<Integer> orderableShopIds = shops.stream().map(OrderableShopBaseInfo::orderableShopId).toList();

        Map<Integer, String> thumbnailImageMap = orderableShopSearchService.findThumbnailUrlsByOrderableShopIds(orderableShopIds);
        Map<Integer, List<String>> containMenuNameMap = orderableShopSearchService.findMatchingMenuNamesByOrderableShopIds(orderableShopIds, processedKeyword);
        Map<Integer, OrderableShopOpenStatus> openStatusMap = shopOpenScheduleService.determineShopOpenStatuses(shops);

        return new DetailInfo(thumbnailImageMap, containMenuNameMap, openStatusMap);
    }

    private record DetailInfo(
        Map<Integer, String> thumbnailImageMap,
        Map<Integer, List<String>> containMenuNameMap,
        Map<Integer, OrderableShopOpenStatus> openStatusMap
    ) {}
}
