package in.koreatech.koin.domain.community.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
import in.koreatech.koin.domain.community.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleKeyword;
import in.koreatech.koin.domain.community.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
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

    public static final int NOTICE_BOARD_ID = 4;

    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final Sort ARTICLES_SORT = Sort.by(Sort.Direction.DESC, "id");

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;
    private final ArticleKeywordRepository articleKeywordRepository;

    @Transactional
    public ArticleResponse getArticle(Integer boardId, Integer articleId) {
        Article article = articleRepository.getById(articleId);
        // article.increaseHit();
        Board board = getBoard(boardId, article);
        Article prevArticle = articleRepository.getPreviousArticle(board, article);
        Article nextArticle = articleRepository.getNextArticle(board, article);
        article.setPrevNextArticles(prevArticle, nextArticle);
        return ArticleResponse.of(article);
    }

    private Board getBoard(Integer boardId, Article article) {
        if (boardId == null) {
            boardId = article.getBoard().getId();
        }
        if (!Objects.equals(boardId, article.getBoard().getId())
            && (!article.getBoard().isNotice() || boardId != NOTICE_BOARD_ID)) {
            throw ArticleBoardMisMatchException.withDetail("boardId: " + boardId + ", articleId: " + article.getId());
        }
        return boardRepository.getById(boardId);
    }

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        if (boardId == NOTICE_BOARD_ID) {
            Page<Article> articles = articleRepository.findAllByBoardIsNoticeIsTrue(pageRequest);
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
        } else if (boardId == NOTICE_BOARD_ID) {
            articles = articleRepository.findAllByBoardIsNoticeIsTrueAndTitleContaining(query, pageRequest);
        } else {
            articles = articleRepository.findAllByBoardIdAndTitleContaining(boardId, query, pageRequest);
        }
        return ArticlesResponse.of(articles, criteria);
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
