package in.koreatech.koin.domain.community.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordExtractor {

    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public List<ArticleKeywordEvent> matchKeyword(List<Article> articles, Integer authorId) {
        List<ArticleKeyword> keywords = articleKeywordRepository.findAll();

        if (keywords.isEmpty()) {
            return List.of();
        }

        List<Integer> keywordIds = keywords.stream()
            .map(ArticleKeyword::getId)
            .toList();
        Map<Integer, List<ArticleKeywordUserMap>> userMapsByKeywordId = articleKeywordUserMapRepository
            .findAllByArticleKeywordIdIn(keywordIds)
            .stream()
            .filter(keywordUserMap -> !keywordUserMap.getIsDeleted())
            .collect(Collectors.groupingBy(
                keywordUserMap -> keywordUserMap.getArticleKeyword().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        Map<Integer, Map<Integer, String>> matchedKeywordByUserIdByArticleId = new LinkedHashMap<>();
        for (Article article : articles) {
            String title = article.getTitle();
            for (ArticleKeyword keyword : keywords) {
                if (!title.contains(keyword.getKeyword())) {
                    continue;
                }
                Map<Integer, String> matchedKeywordByUserId = matchedKeywordByUserIdByArticleId
                    .computeIfAbsent(article.getId(), ignored -> new LinkedHashMap<>());

                for (ArticleKeywordUserMap keywordUserMap :
                    userMapsByKeywordId.getOrDefault(keyword.getId(), List.of())) {
                    Integer userId = keywordUserMap.getUser().getId();
                    matchedKeywordByUserId.merge(
                        userId,
                        keyword.getKeyword(),
                        this::pickHigherPriorityKeyword
                    );
                }
            }
        }

        List<ArticleKeywordEvent> keywordEvents = new ArrayList<>();
        for (Article article : articles) {
            Map<Integer, String> matchedKeywordByUserId = matchedKeywordByUserIdByArticleId.get(article.getId());
            if (matchedKeywordByUserId != null && !matchedKeywordByUserId.isEmpty()) {
                keywordEvents.add(new ArticleKeywordEvent(article.getId(), authorId, matchedKeywordByUserId));
            }
        }

        return keywordEvents;
    }

    private String pickHigherPriorityKeyword(String previousKeyword, String candidateKeyword) {
        if (candidateKeyword.length() > previousKeyword.length()) {
            return candidateKeyword;
        }
        return previousKeyword;
    }
}
