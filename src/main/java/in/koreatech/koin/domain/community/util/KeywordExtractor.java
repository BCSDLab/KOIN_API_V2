package in.koreatech.koin.domain.community.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.KeywordMatchResult;

@Component
public class KeywordExtractor {

    public List<KeywordMatchResult> matchKeyword(
        List<Article> articles, List<ArticleKeywordUserMap> articleKeywordUserMaps, Integer authorId
    ) {
        Set<KeywordMatchResult> keywordMatchResults = new HashSet<>();

        for (Article article : articles) {
            for (ArticleKeywordUserMap articleKeywordUserMap : articleKeywordUserMaps) {
                if (isMatchable(article, articleKeywordUserMap, authorId)) {
                    addOrUpdateResult(keywordMatchResults, article, articleKeywordUserMap);
                }
            }
        }

        return keywordMatchResults.stream().toList();
    }

    private boolean isMatchable(Article article, ArticleKeywordUserMap articleKeywordUserMap, Integer authorId) {
        return !Objects.equals(articleKeywordUserMap.getUserId(), authorId)
            && article.getTitle().contains(articleKeywordUserMap.getKeyword());
    }

    private void addOrUpdateResult(
        Set<KeywordMatchResult> keywordMatchResults, Article article, ArticleKeywordUserMap articleKeywordUserMap
    ) {
        KeywordMatchResult keywordMatchResult = KeywordMatchResult.of(
            article.getId(), articleKeywordUserMap.getUserId(), articleKeywordUserMap.getKeyword()
        );

        keywordMatchResults.stream()
            .filter(result -> result.equals(keywordMatchResult))
            .findFirst()
            .ifPresentOrElse(
                existing -> existing.updateKeywordIfLonger(articleKeywordUserMap.getKeyword()),
                () -> keywordMatchResults.add(keywordMatchResult)
            );
    }
}
