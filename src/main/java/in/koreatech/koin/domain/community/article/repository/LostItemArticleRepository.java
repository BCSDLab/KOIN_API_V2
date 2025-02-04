package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;

public interface LostItemArticleRepository extends Repository<LostItemArticle, Integer> {

    Optional<LostItemArticle> findById(Integer id);

    KoinArticle save(LostItemArticle article);

    default LostItemArticle getById(Integer articleId) {
        return findById(articleId).orElseThrow(/**/);
    }

    Optional<Article> findByIdAndIsDeleted(Integer noticeId, boolean isDeleted);

    @Query(value = "SELECT * FROM new_articles WHERE id = :noticeId", nativeQuery = true)
    Optional<Article> findNoticeById(@Param("noticeId") Integer noticeId);

    @Query(value = "SELECT * FROM new_articles WHERE board_id = :boardId AND is_deleted = :isDeleted", nativeQuery = true)
    Page<Article> findAllByBoardIdAndIsDeleted(@Param("boardId") Integer boardId, @Param("isDeleted") boolean isDeleted, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM new_articles WHERE is_deleted = :isDeleted AND board_id = :boardId", nativeQuery = true)
    Integer countAllByIsDeletedAndBoardId(@Param("isDeleted") boolean isDeleted, @Param("boardId") Integer boardId);

    default Article getNoticeById(Integer noticeId) {
        return findNoticeById(noticeId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("articleId: " + noticeId));
    }

    @Query(value = "SELECT * FROM lost_item_articles WHERE article_id = :articleId AND is_deleted = false", nativeQuery = true)
    Optional<LostItemArticle> findByArticleId(@Param("articleId") Integer articleId);

    default LostItemArticle getByArticleId(Integer articleId) {
        return findByArticleId(articleId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("articleId: " + articleId));
    }
}
