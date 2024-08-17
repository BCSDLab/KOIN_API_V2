package in.koreatech.koin.domain.community.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleKeyword;
import in.koreatech.koin.domain.community.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.model.ArticleViewLog;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.BoardTag;
import in.koreatech.koin.domain.community.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final Sort ARTICLES_SORT = Sort.by(Sort.Direction.DESC, "id");

    private final ArticleRepository articleRepository;
    private final ArticleViewLogRepository articleViewLogRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;

    @Transactional
    public ArticleResponse getArticle(Integer userId, Integer articleId, String ipAddress) {
        Article article = articleRepository.getById(articleId);
        if (isHittable(articleId, userId, ipAddress)) {
            article.increaseHit();
        }
        article.getComment().forEach(comment -> comment.updateAuthority(userId));
        return ArticleResponse.of(article);
    }

    private boolean isHittable(Integer articleId, Integer userId, String ipAddress) {
        if (userId == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        Optional<ArticleViewLog> foundLog = articleViewLogRepository.findByArticleIdAndUserId(articleId, userId);
        if (foundLog.isEmpty()) {
            articleViewLogRepository.save(
                ArticleViewLog.builder()
                    .article(articleRepository.getById(articleId))
                    .user(userRepository.getById(userId))
                    .ip(ipAddress)
                    .build()
            );
            return true;
        }
        if (now.isAfter(foundLog.get().getExpiredAt())) {
            foundLog.get().updateExpiredTime();
            return true;
        }
        return false;
    }

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);

        if (board.isNotice() && board.getTag().equals(BoardTag.공지사항.getTag())) {
            Page<Article> articles = articleRepository.findByIsNotice(true, pageRequest);
            return ArticlesResponse.of(articles, criteria);
        }

        Page<Article> articles = articleRepository.findByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles, criteria);
    }

    public List<HotArticleItemResponse> getHotArticles() {
        PageRequest pageRequest = PageRequest.of(0, HOT_ARTICLE_LIMIT, ARTICLES_SORT);
        return articleRepository.findAll(pageRequest).stream()
            .sorted(Comparator.comparing(Article::getHit).reversed())
            .map(HotArticleItemResponse::from)
            .toList();
    }

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
