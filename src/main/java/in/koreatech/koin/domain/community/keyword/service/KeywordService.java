package in.koreatech.koin.domain.community.keyword.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
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

    @Transactional
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

    public ArticleKeywordsSuggestionResponse suggestKeywords(Integer userId) {
        List<ArticleKeywordSuggestCache> hotKeywords = articleKeywordSuggestRepository.findTop15ByOrderByCountDesc();
        List<String> userKeywords = Optional.ofNullable(articleKeywordUserMapRepository.findAllKeywordbyUserId(userId))
            .orElse(Collections.emptyList());

        List<String> suggestions = hotKeywords.stream()
            .map(ArticleKeywordSuggestCache::getKeyword)
            .filter(keyword -> !userKeywords.contains(keyword))
            .limit(5)
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
        if (keyword.contains(" ")) {
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
    public void fetchTopKeywordsFromLastWeek() {
        Pageable top15 = PageRequest.of(0, 15);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<ArticleKeywordResult> topKeywords = articleKeywordRepository.findTopKeywordsInLastWeek(oneWeekAgo, top15);

        if(topKeywords.isEmpty()) {
            topKeywords = articleKeywordRepository.findTop15Keywords(top15);
        }
        List<ArticleKeywordSuggestCache> hotKeywords = topKeywords.stream()
            .map(result -> ArticleKeywordSuggestCache.builder()
                .hotKeywordId(result.hotKeywordId())
                .keyword(result.keyword())
                .count(result.count().intValue())
                .build())
            .toList();

        articleKeywordSuggestRepository.deleteAll();

        for(ArticleKeywordSuggestCache hotKeyword : hotKeywords) {
            articleKeywordSuggestRepository.save(hotKeyword);
        }
    }
}
