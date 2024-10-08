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
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;

public interface ArticleRepository extends Repository<Article, Integer> {

    Article save(Article article);

    Page<Article> findAllByIsNoticeIsTrue(Pageable pageable);

    Optional<Article> findById(Integer articleId);

    List<Article> findAll(Pageable pageable);

    Page<Article> findAllByBoardId(Integer boardId, PageRequest pageRequest);

    default Article getById(Integer articleId) {
        return findById(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("articleId: " + articleId));
    }

    @Query(
        value = "SELECT * FROM new_articles WHERE board_id = :boardId AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        countQuery = "SELECT count(*) FROM new_articles WHERE board_id = :boardId AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        nativeQuery = true
    )
    Page<Article> findAllByBoardIdAndTitleContaining(@Param("boardId") Integer boardId, @Param("query") String query, Pageable pageable);

    @Query(
        value = "SELECT * FROM new_articles WHERE MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        countQuery = "SELECT count(*) FROM new_articles WHERE MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        nativeQuery = true
    )
    Page<Article> findAllByTitleContaining(@Param("query") String query, Pageable pageable);

    @Query(
        value = "SELECT * FROM new_articles WHERE is_notice = true AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        countQuery = "SELECT count(*) FROM new_articles WHERE is_notice = true AND MATCH(title) AGAINST(CONCAT(:query, '*') IN BOOLEAN MODE)",
        nativeQuery = true
    )
    Page<Article> findAllByIsNoticeIsTrueAndTitleContaining(@Param("query") String query, Pageable pageable);

    Long countBy();

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id < :articleId AND a.is_notice = true "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id < :articleId AND a.board_id = :boardId "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousArticle(@Param("articleId") Integer articleId, @Param("boardId") Integer boardId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id > :articleId AND a.is_notice = true "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM new_articles a "
        + "WHERE a.id > :articleId AND a.board_id = :boardId "
        + "ORDER BY a.id ASC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextArticle(@Param("articleId") Integer articleId, @Param("boardId") Integer boardId);

    default Article getPreviousArticle(Board board, Article article) {
        if (board.isNotice() && board.getId().equals(NOTICE_BOARD_ID)) {
            return findPreviousNoticeArticle(article.getId()).orElse(null);
        }
        return findPreviousArticle(article.getId(), board.getId()).orElse(null);
    }

    default Article getNextArticle(Board board, Article article) {
        if (board.isNotice() && board.getId().equals(NOTICE_BOARD_ID)) {
            return findNextNoticeArticle(article.getId()).orElse(null);
        }
        return findNextArticle(article.getId(), board.getId()).orElse(null);
    }

    @Query(value = "SELECT * FROM new_koreatech_articles ka WHERE ka.registered_at > :registeredAt "
        + "ORDER BY (a.hit + a.koin_hit) DESC, a.registered_at DESC, a.id DESC "
        + "LIMIT :limit", nativeQuery = true)
    List<Article> findAllHotArticlesOld(@Param("registeredAt") LocalDate registeredAt, @Param("limit") int limit);

    @Query(value = "SELECT a.* FROM new_koreatech_articles ka "
        + "JOIN new_articles a ON ka.article_id = a.id "
        + "WHERE ka.registered_at > :registeredAt "
        + "ORDER BY (a.hit + ka.portal_hit) DESC, ka.registered_at DESC, a.id DESC "
        + "LIMIT :limit", nativeQuery = true)
    List<Article> findAllHotArticles(@Param("registeredAt") LocalDate registeredAt, @Param("limit") int limit);

    @Query(value = "SELECT a.* FROM new_koreatech_articles ka "
        + "JOIN new_articles a ON ka.article_id = a.id "
        + "WHERE ka.registered_at > :localDate", nativeQuery = true)
    List<Article> findAllByRegisteredAtIsAfter(LocalDate localDate);

}
