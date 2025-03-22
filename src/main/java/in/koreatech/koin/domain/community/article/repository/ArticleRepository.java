package in.koreatech.koin.domain.community.article.repository;

import static in.koreatech.koin.domain.community.article.service.ArticleService.NOTICE_BOARD_ID;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.exception.BoardNotFoundException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import jakarta.persistence.EntityNotFoundException;

public interface ArticleRepository extends Repository<Article, Integer> {

    Article save(Article article);

    Page<Article> findAllByIsNoticeIsTrue(Pageable pageable);

    Optional<Article> findById(Integer articleId);

    Page<Article> findAll(Pageable pageable);

    Page<Article> findAllByBoardId(Integer boardId, PageRequest pageRequest);

    Page<Article> findAllByIdIn(List<Integer> articleIds, PageRequest pageRequest);

    List<Article> findAllByIdIn(List<Integer> articleIds);

    default Article getById(Integer articleId) {
        Article found = findById(articleId)
            .orElseThrow(() -> ArticleNotFoundException.withDetail("articleId: " + articleId));
        try {
            found.getBoard().getName();
        } catch (EntityNotFoundException e) {
            throw BoardNotFoundException.withDetail("articleId: " + articleId);
        }
        return found;
    }

    @Query(
        value = "SELECT a.* FROM new_articles a JOIN lost_item_articles la ON a.id = la.article_id WHERE la.type = :type AND a.is_deleted = 0",
        countQuery = "SELECT COUNT(*) FROM new_articles a JOIN lost_item_articles la ON a.id = la.article_id WHERE la.type = :type AND a.is_deleted = 0",
        nativeQuery = true
    )
    Page<Article> findAllByLostItemArticleType(@Param("type") String type, PageRequest pageRequest);

    @Query(
        value = "SELECT * FROM new_articles WHERE board_id = :boardId AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        countQuery = "SELECT count(*) FROM new_articles WHERE board_id = :boardId AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        nativeQuery = true
    )
    Page<Article> findAllByBoardIdAndTitleContaining(@Param("boardId") Integer boardId, @Param("query") String query, Pageable pageable);

    @Query(
        value = "SELECT * FROM new_articles WHERE MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        countQuery = "SELECT count(*) FROM new_articles WHERE MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        nativeQuery = true
    )
    Page<Article> findAllByTitleContaining(@Param("query") String query, Pageable pageable);

    @Query(
        value = "SELECT * FROM new_articles WHERE is_notice = true AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        countQuery = "SELECT count(*) FROM new_articles WHERE is_notice = true AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE) AND is_deleted = false",
        nativeQuery = true
    )
    Page<Article> findAllByIsNoticeIsTrueAndTitleContaining(@Param("query") String query, Pageable pageable);

    Long countBy();

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id < :articleId AND a.is_notice = true AND a.is_deleted = false "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id < :articleId AND a.board_id = :boardId AND a.is_deleted = false "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousArticle(@Param("articleId") Integer articleId, @Param("boardId") Integer boardId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id < :articleId AND a.is_deleted = false "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousAllArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id > :articleId AND a.is_notice = true AND a.is_deleted = false "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id > :articleId AND a.board_id = :boardId AND a.is_deleted = false "
        + "ORDER BY a.id ASC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextArticle(@Param("articleId") Integer articleId, @Param("boardId") Integer boardId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id > :articleId AND a.is_deleted = false "
        + "ORDER BY a.id ASC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextAllArticle(@Param("articleId") Integer articleId);

    default Article getPreviousArticle(Board board, Article article) {
        if (board.isNotice() && board.getId().equals(NOTICE_BOARD_ID)) {
            return findPreviousNoticeArticle(article.getId()).orElse(null);
        }
        return findPreviousArticle(article.getId(), board.getId()).orElse(null);
    }

    default Article getPreviousAllArticle(Article article) {
        return findPreviousAllArticle(article.getId()).orElse(null);
    }

    default Article getNextArticle(Board board, Article article) {
        if (board.isNotice() && board.getId().equals(NOTICE_BOARD_ID)) {
            return findNextNoticeArticle(article.getId()).orElse(null);
        }
        return findNextArticle(article.getId(), board.getId()).orElse(null);
    }

    default Article getNextAllArticle(Article article) {
        return findNextAllArticle(article.getId()).orElse(null);
    }

    @Query(value = "SELECT a.* FROM new_articles a "
        + "LEFT JOIN new_koreatech_articles ka ON ka.article_id = a.id "
        + "WHERE ( "
        + "    (ka.article_id IS NOT NULL AND ka.registered_at > :registeredAt) "
        + "    OR (ka.article_id IS NULL AND a.created_at > :registeredAt) "
        + ") "
        + "AND a.is_deleted = false "
        + "ORDER BY (a.hit + IFNULL(ka.portal_hit, 0)) DESC, a.id DESC LIMIT :limit", nativeQuery = true)
    List<Article> findMostHitArticles(@Param("registeredAt") LocalDate registeredAt, @Param("limit") int limit);

    @Query(value = "SELECT a.* FROM new_articles a "
        + "LEFT JOIN new_koreatech_articles ka ON ka.article_id = a.id "
        + "WHERE ( "
        + "    (ka.article_id IS NOT NULL AND ka.registered_at > :registeredAt) "
        + "    OR (ka.article_id IS NULL AND a.created_at > :registeredAt) "
        + ") "
        + "AND a.is_deleted = false ", nativeQuery = true)
    List<Article> findAllByRegisteredAtIsAfter(LocalDate registeredAt);

    @Query("SELECT a.title FROM Article a WHERE a.id = :id")
    String getTitleById(@Param("id") Integer id);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.title REGEXP '통학버스|등교버스|셔틀버스|하교버스' AND a.is_notice = true "
        + "ORDER BY a.created_at DESC LIMIT 5", nativeQuery = true)
    List<Article> findBusArticlesTop5OrderByCreatedAtDesc();
}
