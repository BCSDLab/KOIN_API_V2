package in.koreatech.koin.domain.community.articles.service;

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

import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keywords.dto.ArticleKeywordResponse;
import in.koreatech.koin.domain.community.articles.dto.ArticleResponse;
import in.koreatech.koin.domain.community.articles.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.articles.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.keywords.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.articles.model.Article;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.articles.model.ArticleViewLog;
import in.koreatech.koin.domain.community.articles.model.Board;
import in.koreatech.koin.domain.community.articles.model.BoardTag;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.articles.repository.ArticleRepository;
import in.koreatech.koin.domain.community.articles.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.articles.repository.BoardRepository;
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

        if (isFullNoticeBoard(board)) {
            Page<Article> articles = articleRepository.findAllByIsNotice(true, pageRequest);
            return ArticlesResponse.of(articles, criteria);
        }

        Page<Article> articles = articleRepository.findAllByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles, criteria);
    }

    public List<HotArticleItemResponse> getHotArticles() {
        PageRequest pageRequest = PageRequest.of(0, HOT_ARTICLE_LIMIT, ARTICLES_SORT);
        return articleRepository.findAll(pageRequest).stream()
            .sorted(Comparator.comparing(Article::getHit).reversed())
            .map(HotArticleItemResponse::from)
            .toList();
    }

    public ArticlesResponse searchArticles(String query, Integer boardId, Integer page, Integer limit) {
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles;
        if (boardId == null) {
            articles = articleRepository.findAllByTitleContaining(query, pageRequest);
        } else if (isFullNoticeBoard(boardRepository.getById(boardId))) {
            articles = articleRepository.findAllByIsNoticeAndTitleContaining(true, query, pageRequest);
        } else {
            articles = articleRepository.findAllByBoardIdAndTitleContaining(boardId, query, pageRequest);
        }
        return ArticlesResponse.of(articles, criteria);
    }

    private boolean isFullNoticeBoard(Board board) {
        return board.isNotice() && Objects.equals(board.getTag(), BoardTag.공지사항.getTag());
    }
}
