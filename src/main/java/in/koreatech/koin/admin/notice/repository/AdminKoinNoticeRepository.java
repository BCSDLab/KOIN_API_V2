package in.koreatech.koin.admin.notice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminKoinNoticeRepository extends Repository<Article, Integer> {

    Article save(Article article);

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
}
