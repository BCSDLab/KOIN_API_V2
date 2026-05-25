package in.koreatech.koin.domain.community.keyword.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleKeywordUserMatcher {

    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public Map<Integer, String> findKeywordsByUserId(
        KeywordCategory category,
        List<String> matchedKeywords
    ) {
        Map<Integer, ArticleKeyword> keywordByUserId = new LinkedHashMap<>();
        List<ArticleKeywordUserMap> keywordUserMaps = articleKeywordUserMapRepository
            .findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(category, matchedKeywords);

        for (ArticleKeywordUserMap keywordUserMap : keywordUserMaps) {
            Integer userId = keywordUserMap.getUser().getId();
            ArticleKeyword keyword = keywordUserMap.getArticleKeyword();
            ArticleKeyword previousKeyword = keywordByUserId.get(userId);

            if (keyword.hasLongerKeywordThan(previousKeyword)) {
                keywordByUserId.put(userId, keyword);
            }
        }

        Map<Integer, String> keywordByUserIdResult = new LinkedHashMap<>();
        for (Map.Entry<Integer, ArticleKeyword> entry : keywordByUserId.entrySet()) {
            Integer userId = entry.getKey();
            String keyword = entry.getValue().getKeyword();
            keywordByUserIdResult.put(userId, keyword);
        }
        return keywordByUserIdResult;
    }
}
