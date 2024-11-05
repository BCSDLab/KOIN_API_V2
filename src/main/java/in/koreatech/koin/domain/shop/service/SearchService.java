package in.koreatech.koin.domain.shop.service;

import in.koreatech.koin.domain.shop.dto.search.RelatedKeyword;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

    private final MenuRepository menuRepository;
    private final ShopRepository shopRepository;

    public RelatedKeyword getRelatedKeywordByQuery(String prefix) {
        List<String> menuNames = menuRepository.findDistinctNameStartingWith(prefix);
        List<String> shopNames = shopRepository.findDistinctNameStartingWith(prefix);
        return RelatedKeyword.from(Stream.concat(menuNames.stream(), shopNames.stream()).toList());
    }
}
