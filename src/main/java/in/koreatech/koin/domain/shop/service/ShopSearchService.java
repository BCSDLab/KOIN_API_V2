package in.koreatech.koin.domain.shop.service;

import in.koreatech.koin.domain.shop.cache.ShopsCacheService;
import in.koreatech.koin.domain.shop.cache.dto.ShopCache;
import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.dto.search.response.RelatedKeywordResponse;
import in.koreatech.koin.domain.shop.dto.search.response.RelatedKeywordResponse.InnerKeyword;
import in.koreatech.koin.domain.shop.repository.menu.MenuSearchKeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopSearchService {

    private final MenuSearchKeywordRepository menuSearchKeywordRepository;
    private final ShopsCacheService shopsCacheService;

    private static final String BLANK = " ";
    private static final String EMPTY = "";

    public RelatedKeywordResponse getRelatedKeywordByQuery(String query) {
        String trimmedQuery = removeBlank(query);
        ShopsCache shops = shopsCacheService.findAllShopCache();
        List<InnerKeyword> relatedMenuKeywords = findAllRelatedMenuKeyword(trimmedQuery, shops);
        List<InnerKeyword> relatedShopNames = findAllRelatedShopsKeyword(trimmedQuery, shops);
        return RelatedKeywordResponse.of(relatedMenuKeywords, relatedShopNames);
    }

    private List<InnerKeyword> findAllRelatedMenuKeyword(String query, ShopsCache shops) {
        List<String> menuKeywords = menuSearchKeywordRepository.findDistinctNameContains(query);
        return menuKeywords.stream()
                .map(keyword -> new InnerKeyword(
                        keyword,
                        relatedShopIds(keyword, shops),
                        null
                )).toList();
    }

    private List<Integer> relatedShopIds(String keyword, ShopsCache shops) {
        return shops.shopCaches().stream()
                .filter(shop ->
                        shop.menuNames().stream().anyMatch(menuName -> menuName.contains(keyword)) ||
                        shop.name().contains(keyword))
                .map(ShopCache::id)
                .toList();
    }

    private List<InnerKeyword> findAllRelatedShopsKeyword(String query, ShopsCache shops) {
        return shops.shopCaches().stream()
                .filter(shop -> shop.name().contains(query))
                .map(shop -> new InnerKeyword(
                        shop.name(),
                        List.of(),
                        shop.id()
                )).toList();
    }

    private String removeBlank(String query) {
        return query.replaceAll(BLANK, EMPTY);
    }
}
