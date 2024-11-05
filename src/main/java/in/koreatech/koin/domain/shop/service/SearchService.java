package in.koreatech.koin.domain.shop.service;

import in.koreatech.koin.domain.shop.dto.search.RelatedKeyword;
import in.koreatech.koin.domain.shop.repository.menu.MenuSearchKeywordRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

    private final MenuSearchKeywordRepository menuSearchKeywordRepository;
    private final ShopRepository shopRepository;

    public RelatedKeyword getRelatedKeywordByQuery(String query) {
        String trimmedQuery = query.replaceAll(" ", "");
        List<String> menuKeywords = menuSearchKeywordRepository.findDistinctNameStartingWith(trimmedQuery);
        List<String> shopNames = shopRepository.findDistinctNameStartingWith(trimmedQuery);
        return RelatedKeyword.from(menuKeywords, shopNames);
    }
}
