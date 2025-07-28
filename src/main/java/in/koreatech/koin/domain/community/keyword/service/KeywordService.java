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
import in.koreatech.koin.domain.community.keyword.exception.KeywordDuplicationException;
import in.koreatech.koin.domain.community.keyword.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.common.event.ArticleKeywordEvent;
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
import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
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

    @Transactional
    public ArticleKeywordResponse createKeyword(Integer userId, ArticleKeywordCreateRequest request) {
        String keyword = validateAndGetKeyword(request.keyword());

        validateKeywordLimit(userId);

        ArticleKeyword existingKeyword = findOrRestoreKeyword(keyword);

        ArticleKeywordUserMap userMap = findOrCreateKeywordMapping(existingKeyword, userId);

        return new ArticleKeywordResponse(userMap.getId(), existingKeyword.getKeyword());
    }

    private void validateKeywordLimit(Integer userId) {
        if (articleKeywordUserMapRepository.countByUserId(userId) >= ARTICLE_KEYWORD_LIMIT) {
            throw KeywordLimitExceededException.withDetail("userId: " + userId);
        }
    }

    @ConcurrencyGuard(lockName = "createKeywordManagement")
    private ArticleKeyword findOrRestoreKeyword(String keyword) {
        return articleKeywordRepository.findByKeywordIncludingDeleted(keyword)
            .map(keywordEntity -> {
                if (keywordEntity.getIsDeleted()) {
                    keywordEntity.restore();
                }
                return keywordEntity;
            })
            .orElseGet(() -> createNewKeyword(keyword));
    }

    private ArticleKeyword createNewKeyword(String keyword) {
        return articleKeywordRepository.save(
            ArticleKeyword.builder()
                .keyword(keyword)
                .lastUsedAt(LocalDateTime.now())
                .build()
        );
    }

    @ConcurrencyGuard(lockName = "createKeywordMappingManagement")
    private ArticleKeywordUserMap findOrCreateKeywordMapping(ArticleKeyword existingKeyword, Integer userId) {
        return articleKeywordUserMapRepository.findByArticleKeywordIdAndUserIdIncludingDeleted(existingKeyword.getId(), userId)
            .map(userMap -> {
                if (!userMap.getIsDeleted()) {
                    throw new KeywordDuplicationException("해당 키워드는 이미 등록되었습니다.");
                }
                userMap.restore();
                return userMap;
            })
            .orElseGet(() -> createNewKeywordMapping(existingKeyword, userId));
    }

    private ArticleKeywordUserMap createNewKeywordMapping(ArticleKeyword keyword, Integer userId) {
        ArticleKeywordUserMap keywordUserMap = ArticleKeywordUserMap.builder()
            .user(userRepository.getById(userId))
            .articleKeyword(keyword)
            .build();

        keyword.addUserMap(keywordUserMap);
        return articleKeywordUserMapRepository.save(keywordUserMap);
    }

    @Transactional
    public void deleteKeyword(Integer userId, Integer keywordUserMapId) {
        ArticleKeywordUserMap articleKeywordUserMap = articleKeywordUserMapRepository.getById(keywordUserMapId);
        if (!Objects.equals(articleKeywordUserMap.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        articleKeywordUserMap.delete();

        boolean isKeywordUsedByOthers = articleKeywordUserMapRepository.existsByArticleKeywordId(articleKeywordUserMap.getArticleKeyword().getId());
        if (!isKeywordUsedByOthers) {
            articleKeywordUserMap.getArticleKeyword().delete();
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
