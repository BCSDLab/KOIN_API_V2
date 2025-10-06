package in.koreatech.koin.admin.lostItem.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminLostItemArticleRepository extends Repository<LostItemArticle, Long> {

    Page<LostItemArticle> findAllByIsDeletedFalse(Pageable pageable);

    Integer count();

    Optional<LostItemArticle> findByArticleId(Integer id);

    default LostItemArticle getByArticleId(Integer id) {
        return findByArticleId(id).orElseThrow(() -> ArticleNotFoundException.withDetail("id: " + id));
    }
}
