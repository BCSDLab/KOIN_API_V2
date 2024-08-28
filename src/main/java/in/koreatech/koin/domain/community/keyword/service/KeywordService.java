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

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordsSuggestionResponse;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordDetectedEvent;
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
        String keyword = request.keyword().trim().toLowerCase();

        if (keyword.contains(" ")) {
            throw new KoinIllegalArgumentException("키워드에 공백을 포함할 수 없습니다.");
        }

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

        if (articleKeywordUserMaps.isEmpty()) {
            return new ArticleKeywordsResponse(0, List.of());
        }

        return ArticleKeywordsResponse.from(articleKeywordUserMaps);
    }

    public ArticleKeywordsSuggestionResponse suggestKeywords(Integer userId) {
        List<ArticleKeywordSuggestCache> hotKeywords = articleKeywordSuggestRepository.findTop15ByOrderByCountDesc();

        List<String> userKeywords = articleKeywordUserMapRepository.findAllKeywordbyUserId(userId);

        return ArticleKeywordsSuggestionResponse.from(hotKeywords, userKeywords);
    }

    public void sendDetectKeywordNotification(KeywordNotificationRequest request) {
        List<Integer> updateNotificationIds = request.updateNotification();

        if(!updateNotificationIds.isEmpty()) {
            List<Article> articles = new ArrayList<>();

            for (Integer id : updateNotificationIds) {
                articles.add(articleRepository.getById(id));
            }

            List<ArticleKeywordDetectedEvent> detectedEvents = matchKeyword(articles);

            if (!detectedEvents.isEmpty()) {
                for (ArticleKeywordDetectedEvent event : detectedEvents) {
                    eventPublisher.publishEvent(event);
                }
            }
        }
    }

    private List<ArticleKeywordDetectedEvent> matchKeyword(List<Article> articles) {
        List<ArticleKeywordDetectedEvent> detectedEvents = new ArrayList<>();
        int offset = 0;
        boolean hasMoreKeywords = true;

        while (hasMoreKeywords) {
            Pageable pageable = PageRequest.of(offset / KEYWORD_BATCH_SIZE, KEYWORD_BATCH_SIZE);
            List<ArticleKeyword> keywords = articleKeywordRepository.findAll(pageable);

            if (keywords.isEmpty()) {
                hasMoreKeywords = false;
            } else {
                for (Article article : articles) {
                    String title = article.getTitle();
                    for (ArticleKeyword keyword : keywords) {
                        if (title.contains(keyword.getKeyword())) {
                            detectedEvents.add(new ArticleKeywordDetectedEvent(article.getId(), keyword));
                        }
                    }
                }
                offset += KEYWORD_BATCH_SIZE;
            }
        }
        return detectedEvents;
    };

    @Transactional
    public void fetchTopKeywordsFromLastWeek() {
        Pageable top15 = PageRequest.of(0, 15);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Object[]> topKeywords = articleKeywordRepository.findTopKeywordsInLastWeek(oneWeekAgo, top15);

        List<ArticleKeywordSuggestCache> hotKeywords = topKeywords.stream()
            .map(result -> ArticleKeywordSuggestCache.builder()
                .hotKeywordId((Integer) result[0])
                .keyword((String) result[1])
                .count(((Long) result[2]).intValue())
                .build())
            .collect(Collectors.toList());

        articleKeywordSuggestRepository.deleteAll();

        for(ArticleKeywordSuggestCache hotKeyword : hotKeywords) {
            articleKeywordSuggestRepository.save(hotKeyword);
        }
    }
}
