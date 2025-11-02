package in.koreatech.koin.domain.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.service.OrderableShopSearchKeywordSanitizer;
import in.koreatech.koin.domain.shop.dto.search.response.ShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.shop.repository.shop.ShopSearchQueryRepository;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopMenuNameKeywordHit;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopNameKeywordHit;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopSearchServiceV2 {

    private final OrderableShopSearchKeywordSanitizer searchKeywordSanitizer;
    private final ShopSearchQueryRepository shopSearchQueryRepository;

    public ShopSearchRelatedKeywordResponse searchRelatedKeyword(String rawKeyword) {
        List<String> processedKeywords = searchKeywordSanitizer.sanitizeToKeywords(rawKeyword);

        List<ShopNameKeywordHit> shopNameResults = shopSearchQueryRepository.findAllShopByKeyword(processedKeywords);
        List<ShopMenuNameKeywordHit> menuNameResults = shopSearchQueryRepository.findAllShopMenuByKeyword(
            processedKeywords);

        return ShopSearchRelatedKeywordResponse.from(rawKeyword, processedKeywords, shopNameResults, menuNameResults);
    }
}
