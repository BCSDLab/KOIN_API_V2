package in.koreatech.koin.domain.community.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.KeywordMatchResult;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordExtractor {

    /**
     * - 하나의 게시글에서 여러 키워드가 매칭 될 경우
     * - 하나의 게시글에서 동일한 키워드가 매칭 될 경우
     * - 여러개의 게시글에서 동일한 키워드가 매칭 될 경우
     */

    private static final int KEYWORD_BATCH_SIZE = 100;

    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public List<KeywordMatchResult> matchKeyword(List<Article> articles, Integer authorId) {
        List<ArticleKeywordUserMap> articleKeywordUserMaps = articleKeywordUserMapRepository.findAll();
        Set<KeywordMatchResult> keywordMatchResults = new HashSet<>();

        for (Article article : articles) {
            String title = article.getTitle();

            for (ArticleKeywordUserMap articleKeywordUserMap : articleKeywordUserMaps) {
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

        /*
        Map<Integer, Map<Integer, String>> matchedKeywordByUserIdByArticleId = new LinkedHashMap<>();
        int offset = 0;

        while (true) {
            Pageable pageable = PageRequest.of(offset / KEYWORD_BATCH_SIZE, KEYWORD_BATCH_SIZE);
            List<ArticleKeyword> keywords = articleKeywordRepository.findAll(pageable);

            if (keywords.isEmpty()) {
                break;
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
            offset += KEYWORD_BATCH_SIZE;
        }

        List<ArticleKeywordEvent> keywordEvents = new ArrayList<>();
        for (Article article : articles) {
            Map<Integer, String> matchedKeywordByUserId = matchedKeywordByUserIdByArticleId.get(article.getId());
            if (matchedKeywordByUserId != null && !matchedKeywordByUserId.isEmpty()) {
                keywordEvents.add(new ArticleKeywordEvent(article.getId(), authorId, matchedKeywordByUserId));
            }
        }
         */

        return null;
    }

    private String pickHigherPriorityKeyword(String previousKeyword, String candidateKeyword) {
        if (candidateKeyword.length() > previousKeyword.length()) {
            return candidateKeyword;
        }
        return previousKeyword;
    }
}
