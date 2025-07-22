package in.koreatech.koin.domain.order.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.repository.search.OrderableShopSearchQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderableShopSearchService {

    private final OrderableShopSearchQueryRepository shopSearchQueryRepository;

    private static final String BLANK = " ";
    private static final String EMPTY = "";

    @Transactional(readOnly = true)
    public OrderableShopSearchRelatedKeywordResponse searchRelatedKeyword(String keyword) {
        String trimmedKeyword = removeBlank(keyword);
        var shopNameSearchResult = shopSearchQueryRepository.findAllOrderableShopByKeyword(trimmedKeyword);
        var menuNameSearchResult = shopSearchQueryRepository.findAllMenuByKeyword(trimmedKeyword);
        return OrderableShopSearchRelatedKeywordResponse.from(shopNameSearchResult, menuNameSearchResult);
    }

    private String removeBlank(String query) {
        return query.replaceAll(BLANK, EMPTY);
    }
}
