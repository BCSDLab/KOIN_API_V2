package in.koreatech.koin.domain.community.keyword.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.ArticleKeywordResult;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsSuggestionResponse;
import in.koreatech.koin.domain.community.keyword.dto.FilteredKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private static final int ARTICLE_KEYWORD_LIMIT = 10;
    private static final int KEYWORD_BATCH_SIZE = 100;

    private final ApplicationEventPublisher eventPublisher;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordSuggestRepository articleKeywordSuggestRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @ConcurrencyGuard(lockName = "createKeyword", waitTime = 7, leaseTime = 5)
    public ArticleKeywordResponse createKeyword(Integer userId, ArticleKeywordCreateRequest request) {
        String keyword = validateAndGetKeyword(request.keyword());
        if (articleKeywordUserMapRepository.countByUserId(userId) >= ARTICLE_KEYWORD_LIMIT) {
            throw KeywordLimitExceededException.withDetail("userId: " + userId);
        }

        ArticleKeyword existingKeyword = articleKeywordRepository.findByKeyword(keyword)
            .orElseGet(() -> articleKeywordRepository.save(
                ArticleKeyword.builder()
                    .keyword(keyword)
                    .lastUsedAt(LocalDateTime.now())
                    .build()
            ));

        ArticleKeywordUserMap keywordUserMap = ArticleKeywordUserMap.builder()
            .user(userRepository.getById(userId))
            .articleKeyword(existingKeyword)
            .build();

        existingKeyword.addUserMap(keywordUserMap);
        articleKeywordUserMapRepository.save(keywordUserMap);

        return new ArticleKeywordResponse(keywordUserMap.getId(), existingKeyword.getKeyword());
    }

    @ConcurrencyGuard(lockName = "deleteKeyword")
    public void deleteKeyword(Integer userId, Integer keywordUserMapId) {
        ArticleKeywordUserMap articleKeywordUserMap = articleKeywordUserMapRepository.getById(keywordUserMapId);
        if (!Objects.equals(articleKeywordUserMap.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        articleKeywordUserMapRepository.deleteById(keywordUserMapId);

        boolean isKeywordUsedByOthers = articleKeywordUserMapRepository.existsByArticleKeywordId(
            articleKeywordUserMap.getArticleKeyword().getId());
        if (!isKeywordUsedByOthers) {
            articleKeywordRepository.deleteById(articleKeywordUserMap.getArticleKeyword().getId());
        }
    }

    public ArticleKeywordsResponse getMyKeywords(Integer userId) {
        List<ArticleKeywordUserMap> articleKeywordUserMaps = articleKeywordUserMapRepository.findAllByUserId(userId);
        return ArticleKeywordsResponse.from(articleKeywordUserMaps);
    }

    public ArticleKeywordsSuggestionResponse suggestKeywords() {
        List<ArticleKeywordSuggestCache> hotKeywords = articleKeywordSuggestRepository.findTop15ByOrderByCountDesc();

        List<String> suggestions = hotKeywords.stream()
            .map(ArticleKeywordSuggestCache::getKeyword)
            .filter(keyword -> {
                ArticleKeyword articleKeyword = articleKeywordRepository.findByKeyword(keyword).orElse(null);
                return articleKeyword == null || Boolean.FALSE.equals(articleKeyword.getIsFiltered());
            })
            .collect(Collectors.toList());

        return ArticleKeywordsSuggestionResponse.from(suggestions);
    }

    public void sendKeywordNotification(KeywordNotificationRequest request) {
        List<Integer> updateNotificationIds = request.updateNotification();

        if(!updateNotificationIds.isEmpty()) {
            List<Article> articles = new ArrayList<>();

            for (Integer id : updateNotificationIds) {
                articles.add(articleRepository.getById(id));
            }

            List<ArticleKeywordEvent> keywordEvents = matchKeyword(articles);

            if (!keywordEvents.isEmpty()) {
                for (ArticleKeywordEvent event : keywordEvents) {
                    eventPublisher.publishEvent(event);
                }
            }
        }
    }

    private String validateAndGetKeyword(String keyword) {
        if (keyword.contains(" ") || keyword.contains("\n")) {
            throw new KoinIllegalArgumentException("키워드에 공백을 포함할 수 없습니다.");
        }
        return keyword.trim().toLowerCase();
    }

    private List<ArticleKeywordEvent> matchKeyword(List<Article> articles) {
        List<ArticleKeywordEvent> keywordEvents = new ArrayList<>();
        int offset = 0;

        while (true) {
            Pageable pageable = PageRequest.of(offset / KEYWORD_BATCH_SIZE, KEYWORD_BATCH_SIZE);
            List<ArticleKeyword> keywords = articleKeywordRepository.findAll(pageable);

            if (keywords.isEmpty()) {
                break;
            }

            for (Article article : articles) {
                String title = article.getTitle();
                for (ArticleKeyword keyword : keywords) {
                    if (title.contains(keyword.getKeyword())) {
                        keywordEvents.add(new ArticleKeywordEvent(article.getId(), keyword));
                    }
                }
            }
            offset += KEYWORD_BATCH_SIZE;
        }

        return keywordEvents;
    }

    @Transactional
    public void filterKeyword(String keyword, Boolean isFiltered) {
        ArticleKeyword articleKeyword = articleKeywordRepository.getByKeyword(keyword);

        if (Objects.equals(articleKeyword.getIsFiltered(), isFiltered)) {
            throw new KoinIllegalArgumentException(
                isFiltered ? "이미 필터링 된 키워드입니다: " + keyword : "이미 필터링이 취소된 키워드입니다: " + keyword
            );
        }

        articleKeyword.applyFiltered(isFiltered);
    }

    @Transactional(readOnly = true)
    public FilteredKeywordsResponse getFilteredKeywords() {
        List<ArticleKeyword> filteredKeywords = articleKeywordRepository.findByIsFiltered(true);
        return FilteredKeywordsResponse.from(filteredKeywords);
    }

    @Transactional
    public void fetchTopKeywordsFromLastWeek() {
        List<ArticleKeywordResult> topKeywords = fetchKeywordsFromLastWeek();

        List<ArticleKeywordResult> filteredKeywords = excludeFilteredKeywords(topKeywords);

        List<ArticleKeywordResult> completeKeywords = completeKeywordList(filteredKeywords, 15);

        List<ArticleKeywordSuggestCache> hotKeywords = createHotKeywordCache(completeKeywords);
        updateKeywordCache(hotKeywords);
    }


    private List<ArticleKeywordResult> fetchKeywordsFromLastWeek() {
        Pageable top15 = PageRequest.of(0, 15);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        return articleKeywordRepository.findTopKeywordsInLastWeek(oneWeekAgo, top15);
    }

    private List<ArticleKeywordResult> excludeFilteredKeywords(List<ArticleKeywordResult> keywords) {
        return keywords.stream()
            .filter(result -> {
                ArticleKeyword keyword = articleKeywordRepository.findByKeyword(result.keyword()).orElse(null);
                return keyword == null || Boolean.FALSE.equals(keyword.getIsFiltered());
            })
            .toList();
    }

    private List<ArticleKeywordResult> completeKeywordList(List<ArticleKeywordResult> currentKeywords, int targetCount) {
        int remainingCount = targetCount - currentKeywords.size();

        while (remainingCount > 0) {
            List<ArticleKeywordResult> additionalKeywords = fetchAdditionalKeywords(remainingCount);

            List<ArticleKeywordResult> filteredAdditionalKeywords = excludeFilteredKeywords(additionalKeywords);

            currentKeywords = Stream.concat(currentKeywords.stream(), filteredAdditionalKeywords.stream())
                .distinct()
                .limit(targetCount)
                .toList();

            remainingCount = targetCount - currentKeywords.size();

            if (filteredAdditionalKeywords.isEmpty()) {
                break;
            }
        }

        return currentKeywords;
    }

    private List<ArticleKeywordResult> fetchAdditionalKeywords(int count) {
        Pageable pageable = PageRequest.of(0, count);
        return articleKeywordRepository.findTop15Keywords(pageable);
    }

    private List<ArticleKeywordSuggestCache> createHotKeywordCache(List<ArticleKeywordResult> topKeywords) {
        return topKeywords.stream()
            .filter(result -> {
                ArticleKeyword keyword = articleKeywordRepository.findByKeyword(result.keyword()).orElse(null);
                return keyword == null || Boolean.FALSE.equals(keyword.getIsFiltered());
            })
            .map(result -> ArticleKeywordSuggestCache.builder()
                .hotKeywordId(result.hotKeywordId())
                .keyword(result.keyword())
                .count(result.count().intValue())
                .build())
            .toList();
    }

    private void updateKeywordCache(List<ArticleKeywordSuggestCache> hotKeywords) {
        articleKeywordSuggestRepository.deleteAll();
        for(ArticleKeywordSuggestCache hotKeyword : hotKeywords) {
            articleKeywordSuggestRepository.save(hotKeyword);
        }
    }
}
