package in.koreatech.koin.domain.community.article.model;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchLogEventListener {

    private final ArticleSearchKeywordRepository articleSearchKeywordRepository;
    private final ArticleSearchKeywordIpMapRepository articleSearchKeywordIpMapRepository;

    private static final double INITIAL_WEIGHT = 1.0;
    private static final int MAX_DYNAMIC_WEIGHT_COUNT = 5;
    private static final int FIXED_WEIGHT_LIMIT = 10;
    private static final double FIXED_WEIGHT = 1.0 / Math.pow(2, 4);

    @EventListener
    public void handleSearchLogEvent(SearchLogEvent event) {
        String query = event.getQuery();
        String ipAddress = event.getIpAddress();

        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String[] keywords = query.split("\\s+");

        for (String keywordStr : keywords) {
            ArticleSearchKeyword keyword = articleSearchKeywordRepository.findByKeyword(keywordStr)
                .orElseGet(() -> {
                    ArticleSearchKeyword newKeyword = ArticleSearchKeyword.builder()
                        .keyword(keywordStr)
                        .weight(INITIAL_WEIGHT)
                        .lastSearchedAt(LocalDateTime.now())
                        .totalSearch(1)
                        .build();
                    articleSearchKeywordRepository.save(newKeyword);
                    return newKeyword;
                });

            ArticleSearchKeywordIpMap map = articleSearchKeywordIpMapRepository.findByArticleSearchKeywordAndIpAddress(
                    keyword, ipAddress)
                .orElseGet(() -> {
                    ArticleSearchKeywordIpMap newMap = ArticleSearchKeywordIpMap.builder()
                        .articleSearchKeyword(keyword)
                        .ipAddress(ipAddress)
                        .searchCount(1)
                        .build();
                    articleSearchKeywordIpMapRepository.save(newMap);
                    return newMap;
                });

            updateKeywordWeightAndCount(keyword, map);
        }
    }

    private void updateKeywordWeightAndCount(ArticleSearchKeyword keyword, ArticleSearchKeywordIpMap map) {
        map.incrementSearchCount();
        double additionalWeight = calculateWeight(map.getSearchCount());

        if (map.getSearchCount() <= FIXED_WEIGHT_LIMIT) {
            keyword.updateWeight(keyword.getWeight() + additionalWeight);
        }
        articleSearchKeywordRepository.save(keyword);
    }

    private double calculateWeight(int searchCount) {
        if (searchCount <= MAX_DYNAMIC_WEIGHT_COUNT) {
            return 1.0 / Math.pow(2, searchCount - 1);
        } else if (searchCount <= FIXED_WEIGHT_LIMIT) {
            return FIXED_WEIGHT;
        }
        return 0.0;
    }
}
