package in.koreatech.koin.domain.community.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.KeywordMatchResult;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KeywordExtractor {

    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public List<KeywordMatchResult> matchKeyword(List<Article> articles, Integer authorId) {
        List<ArticleKeywordUserMap> articleKeywordUserMaps = articleKeywordUserMapRepository.findAll();
        Set<KeywordMatchResult> keywordMatchResults = new HashSet<>();

        for (Article article : articles) {
            String title = article.getTitle();

            for (ArticleKeywordUserMap articleKeywordUserMap : articleKeywordUserMaps) {
                if (Objects.equals(articleKeywordUserMap.getUserId(), authorId)) {
                    continue;
                }

                String keyword = articleKeywordUserMap.getKeyword();
                if (!title.contains(keyword)) {
                    continue;
                }

                KeywordMatchResult keywordMatchResult = KeywordMatchResult.of(
                    article.getId(), articleKeywordUserMap.getUserId(), keyword
                );

                keywordMatchResults.stream()
                    .filter(result -> result.equals(keywordMatchResult))
                    .findFirst()
                    .ifPresentOrElse(
                        existing -> existing.updateKeywordIfLonger(keyword),
                        () -> keywordMatchResults.add(keywordMatchResult)
                    );
            }
        }

        return keywordMatchResults.stream().toList();
    }
}
