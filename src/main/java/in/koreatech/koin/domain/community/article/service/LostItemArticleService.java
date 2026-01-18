package in.koreatech.koin.domain.community.article.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleStatisticsResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleUpdateRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.model.filter.LostItemAuthorFilter;
import in.koreatech.koin.domain.community.article.model.filter.LostItemFoundStatus;
import in.koreatech.koin.domain.community.article.model.filter.LostItemCategoryFilter;
import in.koreatech.koin.domain.community.article.model.filter.LostItemSortType;
import in.koreatech.koin.domain.community.article.model.redis.PopularKeywordTracker;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemArticleService {

    public static final int LOST_ITEM_BOARD_ID = 14;
    private static final int MAXIMUM_SEARCH_LENGTH = 100;
    public static final int NOTICE_BOARD_ID = 4;
    private static final Sort NATIVE_ARTICLES_SORT = Sort.by(
        Sort.Order.desc("id")
    );
    private static final Sort ARTICLES_SORT = Sort.by(
        Sort.Order.desc("id")
    );

    private final ArticleRepository articleRepository;
    private final LostItemArticleRepository lostItemArticleRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PopularKeywordTracker popularKeywordTracker;
    private final ApplicationEventPublisher eventPublisher;
    private final KeywordExtractor keywordExtractor;

    @Transactional
    public LostItemArticlesResponse searchLostItemArticles(String query, Integer page, Integer limit,
        String ipAddress, Integer userId) {
        if (query.length() >= MAXIMUM_SEARCH_LENGTH) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NATIVE_ARTICLES_SORT);
        Page<Article> articles = articleRepository.findAllByBoardIdAndTitleContaining(LOST_ITEM_BOARD_ID, query,
            pageRequest);

        String[] keywords = query.split("\\s+");

        for (String keyword : keywords) {
            popularKeywordTracker.updateKeywordWeight(ipAddress, keyword);
        }

        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    public LostItemArticlesResponse getLostItemArticles(String type, Integer page, Integer limit, Integer userId) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles;

        if (type == null) {
            articles = articleRepository.findAllByBoardId(LOST_ITEM_BOARD_ID, pageRequest);
        } else {
            articles = articleRepository.findAllByLostItemArticleType(type, pageRequest);
        }

        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    public LostItemArticlesResponse getLostItemArticlesV2(String type, Integer page, Integer limit, Integer userId,
        LostItemFoundStatus foundStatus, LostItemCategoryFilter itemCategory, LostItemSortType sort,
        LostItemAuthorFilter authorType,  String titleQuery) {
        Integer authorIdFilter = authorType.getRequiredAuthorId(userId);

        String refinedTitleQuery = Optional.ofNullable(titleQuery)
            .map(String::trim)
            .orElse(null);

        Boolean foundStatusFilter = Optional.ofNullable(foundStatus)
            .map(LostItemFoundStatus::getQueryStatus)
            .orElse(null);

        String itemCategoryFilter = Optional.ofNullable(itemCategory)
            .filter(category -> category != LostItemCategoryFilter.ALL)
            .map(LostItemCategoryFilter::getStatus)
            .orElse(null);

        Long total = lostItemArticleRepository.countLostItemArticlesWithFilters(type, foundStatusFilter,
            itemCategoryFilter, LOST_ITEM_BOARD_ID, authorIdFilter, refinedTitleQuery);

        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());

        List<Article> articles = lostItemArticleRepository.findLostItemArticlesWithFilters(LOST_ITEM_BOARD_ID, type,
            foundStatusFilter, itemCategoryFilter, sort, pageRequest, authorIdFilter, refinedTitleQuery);
        Page<Article> articlePage = new PageImpl<>(articles, pageRequest, total);

        return LostItemArticlesResponse.of(articlePage, criteria, userId);
    }

    public LostItemArticleResponse getLostItemArticle(Integer articleId, Integer userId) {
        Article article = articleRepository.getById(articleId);
        setPrevNextArticle(LOST_ITEM_BOARD_ID, article);

        boolean isMine = false;
        User author = article.getLostItemArticle().getAuthor();
        if (author != null && Objects.equals(author.getId(), userId)) {
            isMine = true;
        }

        return LostItemArticleResponse.of(article, isMine);
    }

    @Transactional
    public LostItemArticleResponse createLostItemArticle(Integer userId, LostItemArticlesRequest requests) {
        Board lostItemBoard = boardRepository.getById(LOST_ITEM_BOARD_ID);
        User user = userRepository.getById(userId);
        List<Article> newArticles = new ArrayList<>();

        for (var article : requests.articles()) {
            Article lostItemArticle = Article.createLostItemArticle(article, lostItemBoard, user);
            articleRepository.save(lostItemArticle);
            newArticles.add(lostItemArticle);
        }

        sendKeywordNotification(newArticles, userId);
        return LostItemArticleResponse.of(newArticles.get(0), true);
    }

    @Transactional
    public void deleteLostItemArticle(Integer articleId, Integer userId) {
        Article foundArticle = articleRepository.getById(articleId);
        User author = foundArticle.getLostItemArticle().getAuthor();
        if (!Objects.equals(author.getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        foundArticle.delete();
    }

    public LostItemArticleStatisticsResponse getLostItemArticlesStats() {
        Integer foundCount = lostItemArticleRepository.getFoundLostItemArticleCount();
        Integer notFoundCount = lostItemArticleRepository.getNotFoundLostItemArticleCount();
        return new LostItemArticleStatisticsResponse(foundCount, notFoundCount);
    }

    @Transactional
    public void markLostItemArticleAsFound(Integer userId, Integer articleId) {
        LostItemArticle lostItemArticle = articleRepository.getById(articleId).getLostItemArticle();
        lostItemArticle.checkOwnership(userId);
        lostItemArticle.markAsFound();
    }

    @Transactional
    public LostItemArticleResponse updateLostItemArticle(Integer userId, Integer articleId, LostItemArticleUpdateRequest request) {
        LostItemArticle lostItemArticle = lostItemArticleRepository.getByArticleId(articleId);
        lostItemArticle.checkOwnership(userId);

        lostItemArticle.update(
            request.category(),
            request.foundPlace(),
            request.foundDate(),
            request.content()
        );

        lostItemArticle.deleteImages(request.deleteImageIds());
        lostItemArticle.addNewImages(request.newImages());

        return LostItemArticleResponse.of(lostItemArticle.getArticle(), true);
    }

    private void setPrevNextArticle(Integer boardId, Article article) {
        Article prevArticle;
        Article nextArticle;
        if (boardId != null) {
            Board board = getBoard(boardId, article);
            prevArticle = articleRepository.getPreviousArticle(board, article);
            nextArticle = articleRepository.getNextArticle(board, article);
        } else {
            prevArticle = articleRepository.getPreviousAllArticle(article);
            nextArticle = articleRepository.getNextAllArticle(article);
        }
        article.setPrevNextArticles(prevArticle, nextArticle);
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

    private void sendKeywordNotification(List<Article> articles, Integer authorId) {
        List<ArticleKeywordEvent> keywordEvents = keywordExtractor.matchKeyword(articles, authorId);
        if (!keywordEvents.isEmpty()) {
            for (ArticleKeywordEvent event : keywordEvents) {
                eventPublisher.publishEvent(event);
            }
        }
    }
}
