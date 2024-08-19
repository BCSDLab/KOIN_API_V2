package in.koreatech.koin.domain.community.keywords.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.coopShop.repository.AdminCoopShopRepository;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.keywords.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;
    private final UserRepository userRepository;

    @Transactional
    public ArticleKeywordResponse createKeyword(Integer userId, ArticleKeywordCreateRequest request) {
        String keyword = request.keyword().trim().toLowerCase();

        if (keyword.contains(" ")) {
            throw new KoinIllegalArgumentException("키워드에 공백을 포함할 수 없습니다.");
        }

        if (articleKeywordUserMapRepository.countByUserId(userId) >= 10) {
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
}
