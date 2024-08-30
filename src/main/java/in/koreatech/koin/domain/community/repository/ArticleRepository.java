package in.koreatech.koin.domain.community.repository;

import static in.koreatech.koin.domain.community.service.CommunityService.NOTICE_BOARD_ID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;

public interface ArticleRepository extends Repository<Article, Integer> {

    Article save(Article article);

    Page<Article> findAllByIsNoticeIsTrue(Pageable pageable);

    Optional<Article> findById(Integer articleId);

    List<Article> findAll(Pageable pageable);

    Page<Article> findAllByBoardId(Integer boardId, PageRequest pageRequest);

    default Article getById(Integer articleId) {
        return findById(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail(
                "articleId: " + articleId));
    }

    Page<Article> findAllByBoardIdAndTitleContaining(Integer boardId, String query, PageRequest pageRequest);

    Page<Article> findAllByTitleContaining(String query, PageRequest pageRequest);

    Page<Article> findAllByIsNoticeIsTrueAndTitleContaining(String query, PageRequest pageRequest);

    Long countBy();

    @Query(value = "SELECT * FROM articles a "
        + "WHERE a.id < :articleId AND a.is_notice = true "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM articles a "
        + "WHERE a.id < :articleId AND a.board_id = :boardId "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findPreviousArticle(@Param("articleId") Integer articleId, @Param("boardId") Integer boardId);

    @Query(value = "SELECT * FROM articles a "
        + "WHERE a.id > :articleId AND a.is_notice = true "
        + "ORDER BY a.id DESC LIMIT 1", nativeQuery = true)
    Optional<Article> findNextNoticeArticle(@Param("articleId") Integer articleId);

    @Query(value = "SELECT * FROM articles a "
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
}
