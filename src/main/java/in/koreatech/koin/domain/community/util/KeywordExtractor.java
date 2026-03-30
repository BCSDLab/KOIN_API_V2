package in.koreatech.koin.domain.community.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.dto.ArticleSummary;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.KeywordMatchResult;

@Component
public class KeywordExtractor {

    public List<KeywordMatchResult> matchKeyword(
        List<ArticleSummary> articleSummaries, List<ArticleKeywordUserMap> articleKeywordUserMaps, Integer authorId
    ) {
        Set<KeywordMatchResult> keywordMatchResults = new HashSet<>();

        for (ArticleSummary articleSummary : articleSummaries) {
            for (ArticleKeywordUserMap articleKeywordUserMap : articleKeywordUserMaps) {
                if (isMatchable(articleSummary, articleKeywordUserMap, authorId)) {
                    addOrUpdateResult(keywordMatchResults, articleSummary, articleKeywordUserMap);
                }
            }
        }

        return keywordMatchResults.stream().toList();
    }

    private boolean isMatchable(
        ArticleSummary articleSummaries, ArticleKeywordUserMap articleKeywordUserMap, Integer authorId
    ) {
        return !Objects.equals(articleKeywordUserMap.getUserId(), authorId)
            && articleSummaries.title().contains(articleKeywordUserMap.getKeyword());
    }

    private void addOrUpdateResult(
        Set<KeywordMatchResult> keywordMatchResults, ArticleSummary ArticleSummary,
        ArticleKeywordUserMap articleKeywordUserMap
    ) {
        KeywordMatchResult keywordMatchResult = KeywordMatchResult.of(
            ArticleSummary.id(), articleKeywordUserMap.getUserId(), articleKeywordUserMap.getKeyword()
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
