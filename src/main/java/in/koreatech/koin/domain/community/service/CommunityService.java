package in.koreatech.koin.domain.community.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleViewLog;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Criteria;
import in.koreatech.koin.domain.community.model.HotArticle;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.community.repository.HotArticleRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
    private final HotArticleRepository hotArticleRepository;

    @Transactional
    public ArticleResponse getArticle(Long userId, Long articleId, String ipAddress) {
        Article article = articleRepository.getById(articleId);
        if (isHittable(articleId, userId, ipAddress)) {
            hitArticle(article);
        }
        article.getComment().forEach(comment -> comment.updateAuthority(userId));
        return ArticleResponse.of(article);
    }

    private boolean isHittable(Long articleId, Long userId, String ipAddress) {
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

    private void hitArticle(Article article) {
        article.increaseHit();
        HotArticle hotArticle = hotArticleRepository.findById(article.getId());
        if (hotArticle == null) {
            hotArticleRepository.save(HotArticle.from(article.getId()));
            return;
        }
        hotArticle.hit();
        hotArticleRepository.save(hotArticle);
    }

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Criteria criteria = Criteria.of(page, limit);
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles = articleRepository.getByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }

    public List<HotArticleItemResponse> getHotArticles() {
        List<Long> articles = getRecentlyArticlesId();
        List<Long> hotArticlesId = getHotArticlesId();
        hotArticlesId.addAll(articles);

        return hotArticlesId.stream()
            .distinct()
            .limit(HOT_ARTICLE_LIMIT)
            .map(articleRepository::getById)
            .map(HotArticleItemResponse::from)
            .toList();
    }

    private List<Long> getRecentlyArticlesId() {
        PageRequest pageRequest = PageRequest.of(0, HOT_ARTICLE_LIMIT, ARTICLES_SORT);
        return articleRepository.findAll(pageRequest).stream()
            .map(Article::getId)
            .toList();
    }

    private List<Long> getHotArticlesId() {
        return hotArticleRepository.findAll().stream()
            .sorted(Comparator.reverseOrder())
            .limit(HOT_ARTICLE_LIMIT)
            .map(HotArticle::getId)
            .collect(Collectors.toList());
    }

    public void validateHits() {
        List<HotArticle> hotArticles = hotArticleRepository.findAll();
        hotArticles.forEach(HotArticle::validate);
        hotArticles.forEach(hotArticle -> {
            if (hotArticle.isEmpty()) {
                hotArticleRepository.deleteById(hotArticle.getId());
            } else {
                hotArticleRepository.save(hotArticle);
            }
        });
    }
}
