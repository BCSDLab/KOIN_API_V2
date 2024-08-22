package in.koreatech.koin.domain.community.keywords.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordsResponse;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordsSuggestionResponse;
import in.koreatech.koin.domain.community.keywords.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggest;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private static final int ARTICLE_KEYWORD_LIMIT = 10;

    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordSuggestRepository articleKeywordSuggestRepository;
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
        List<ArticleKeywordSuggest> hotKeywords = articleKeywordSuggestRepository.findTop15ByOrderByCountDesc();

        List<String> userKeywords = articleKeywordUserMapRepository.findAllKeywordbyUserId(userId);

        return ArticleKeywordsSuggestionResponse.from(hotKeywords, userKeywords);
    }

    public void fetchTopKeywordsFromLastWeek() {
        Pageable top15 = PageRequest.of(0, 15);
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Object[]> topKeywords = articleKeywordRepository.findTopKeywordsInLastWeek(oneWeekAgo, top15);

        List<ArticleKeywordSuggest> hotKeywords = topKeywords.stream()
            .map(result -> ArticleKeywordSuggest.builder()
                .hotKeywordId((Integer) result[0])
                .keyword((String) result[1])
                .count((Integer) result[2])
                .build())
            .collect(Collectors.toList());

        articleKeywordSuggestRepository.deleteAll();
        articleKeywordSuggestRepository.saveAll(hotKeywords);
    }
}
