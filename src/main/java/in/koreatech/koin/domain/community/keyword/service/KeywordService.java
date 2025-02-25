package in.koreatech.koin.domain.community.keyword.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.UserNotificationStatus;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
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


    private final ApplicationEventPublisher eventPublisher;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordSuggestRepository articleKeywordSuggestRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final KeywordExtractor keywordExtractor;

    @ConcurrencyGuard(lockName = "createKeyword")
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

    @Transactional
    public void deleteKeyword(Integer userId, Integer keywordUserMapId) {
        ArticleKeywordUserMap articleKeywordUserMap = articleKeywordUserMapRepository.getById(keywordUserMapId);
        if (!Objects.equals(articleKeywordUserMap.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        deleteMappingAndUnusedKeywordWithLock(keywordUserMapId, articleKeywordUserMap.getArticleKeyword().getId());
    }

    @ConcurrencyGuard(lockName = "deleteKeyword")
    private void deleteMappingAndUnusedKeywordWithLock(Integer keywordUserMapId, Integer articleKeywordId) {
        articleKeywordUserMapRepository.deleteById(keywordUserMapId);

        boolean isKeywordUsedByOthers = articleKeywordUserMapRepository.existsByArticleKeywordId(articleKeywordId);
        if (!isKeywordUsedByOthers) {
            articleKeywordRepository.deleteById(articleKeywordId);
        }
    }

    public ArticleKeywordsResponse getMyKeywords(Integer userId) {
        List<ArticleKeywordUserMap> articleKeywordUserMaps = articleKeywordUserMapRepository.findAllByUserId(userId);
        return ArticleKeywordsResponse.from(articleKeywordUserMaps);
    }

    public ArticleKeywordsSuggestionResponse suggestKeywords() {
        List<String> suggestions = articleKeywordSuggestRepository.findTop15ByOrderByCountDesc()
            .stream()
            .map(ArticleKeywordSuggestCache::getKeyword)
            .collect(Collectors.toList());

        return ArticleKeywordsSuggestionResponse.from(suggestions);
    }

    public void sendKeywordNotification(KeywordNotificationRequest request) {
        List<Integer> updateNotificationIds = request.updateNotification();

        if (!updateNotificationIds.isEmpty()) {
            List<Article> articles = new ArrayList<>();

            for (Integer id : updateNotificationIds) {
                articles.add(articleRepository.getById(id));
            }

            List<ArticleKeywordEvent> keywordEvents = keywordExtractor.matchKeyword(articles, null);

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

    @Transactional
    public void fetchTopKeywordsFromLastWeek() {
        Pageable top15 = PageRequest.of(0, 15);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<ArticleKeywordResult> topKeywords = articleKeywordRepository.findTopKeywordsInLastWeekExcludingFiltered(oneWeekAgo, top15);

        if (topKeywords.size() < 15) {
            topKeywords = articleKeywordRepository.findTop15KeywordsExcludingFiltered(top15);
        }

        List<ArticleKeywordSuggestCache> hotKeywords = topKeywords.stream()
            .map(result -> ArticleKeywordSuggestCache.builder()
                .hotKeywordId(result.hotKeywordId())
                .keyword(result.keyword())
                .count(result.count().intValue())
                .build())
            .toList();

        articleKeywordSuggestRepository.deleteAll();
        for (ArticleKeywordSuggestCache hotKeyword : hotKeywords) {
            articleKeywordSuggestRepository.save(hotKeyword);
        }
    }

    @Transactional
    public void updateLastNotifiedArticle(Integer userId, Integer articleId) {
        UserNotificationStatus status = userNotificationStatusRepository.findByUserId(userId)
            .orElseGet(() -> new UserNotificationStatus(userId, articleId));

        status.updateLastNotifiedArticleId(articleId);

        userNotificationStatusRepository.save(status);
    }
}
